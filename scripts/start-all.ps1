param(
    [switch]$RestartExisting,
    [switch]$RunSmokeTest,
    [int]$StartupTimeoutSeconds = 240,
    # New switch to control whether to load the Insforge (Postgres) environment
    # If the file config\local-insforge.env exists but you don't want to use it
    # when starting services locally (to avoid unreachable DBs), don't pass -UseInsforge.
    [switch]$UseInsforge,
    # When using Insforge, wait up to this many seconds for the remote Postgres to be reachable
    [int]$InsforgeWaitSeconds = 60
)

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

$services = @(
    [ordered]@{ Name = 'eureka-server';      Path = Join-Path $root 'eureka-server';         Port = 8761; Ready = 'Http'; Url = 'http://localhost:8761';                         Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-gestionpacientes'; Path = Join-Path $root 'ms-gestionpacientes';  Port = 8083; Ready = 'Http'; Url = 'http://localhost:8083/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-notificaciones';   Path = Join-Path $root 'ms-notificaciones';    Port = 8085; Ready = 'Http'; Url = 'http://localhost:8085/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-optimizacion';     Path = Join-Path $root 'ms-optimizacion';      Port = 8084; Ready = 'Http'; Url = 'http://localhost:8084/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'api-gateway';         Path = Join-Path $root 'api-gateway';          Port = 8080; Ready = 'Http'; Url = 'http://localhost:8080/swagger-ui.html';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'salud-bff';           Path = Join-Path $root 'bff';                  Port = 8097; Ready = 'Http'; Url = 'http://localhost:8097/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' }
)

function Test-PortListening {
    param([int]$Port)
    return [bool](Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Stop-PortListener {
    param([int]$Port)
    $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        try {
            Stop-Process -Id $listener.OwningProcess -Force -ErrorAction Stop
            Write-Host "[STOP] Puerto $Port -> PID $($listener.OwningProcess)" -ForegroundColor DarkYellow
        } catch {
            Write-Host "[WARN] No se pudo detener PID $($listener.OwningProcess) en el puerto $Port" -ForegroundColor Yellow
        }
    }
}

function Wait-ForHttp {
    param(
        [string]$Url,
        [int]$TimeoutSeconds,
        [string]$ServiceName
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                Write-Host "[READY] $ServiceName -> $Url ($($response.StatusCode))" -ForegroundColor Green
                return
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    } while ((Get-Date) -lt $deadline)

    throw "Timeout esperando $ServiceName en $Url"
}

function Start-ServiceWindow {
    param(
        [hashtable]$Service
    )

    if ($RestartExisting) {
        Stop-PortListener -Port $Service.Port
    }

    if (Test-PortListening -Port $Service.Port) {
        Write-Host "[SKIP] $($Service.Name) ya ocupa el puerto $($Service.Port)" -ForegroundColor Yellow
        return
    }

    Write-Host "[START] $($Service.Name)" -ForegroundColor Cyan
    # Si existe local-insforge.env y se solicitó explícitamente con -UseInsforge,
    # cada ventana carga DB_* y SPRING_PROFILES_ACTIVE antes de mvnw.
    $loadScript = Join-Path $root 'scripts\load-insforge-env.ps1'
    $startCmd = $Service.Command
    if ($UseInsforge -and (Test-Path (Join-Path $root 'config\local-insforge.env'))) {
        $startCmd = ". `"$loadScript`" -SilentIfMissing; " + $Service.Command
    }
    Start-Process -FilePath powershell.exe `
        -WorkingDirectory $Service.Path `
        -ArgumentList @('-NoLogo', '-NoProfile', '-NoExit', '-Command', $startCmd) | Out-Null
}

Write-Host "=== Levantando todos los servicios ===" -ForegroundColor Cyan
Write-Host "Raiz: $root" -ForegroundColor DarkGray

# If the user requested to use Insforge, verify DB connectivity before starting services.
if ($UseInsforge -and (Test-Path (Join-Path $root 'config\local-insforge.env'))) {
    try {
        $envFile = Join-Path $root 'config\local-insforge.env'
        $lines = Get-Content $envFile | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne '' -and -not $_.StartsWith('#') }
        $dbLine = $lines | Where-Object { $_ -match '^DB_URL=' } | Select-Object -First 1
        if ($null -eq $dbLine) {
            Write-Host "[WARN] No se encontró DB_URL en $envFile; asumiendo puerto 5432" -ForegroundColor Yellow
            $dbHost = 'localhost'
            $dbPort = 5432
        } else {
            $dbUrl = $dbLine -replace '^DB_URL=', ''
            # Extraer host y puerto de una URL JDBC tipo: jdbc:postgresql://host:5432/dbname?params
            if ($dbUrl -match 'jdbc:postgresql:\/\/(?<host>[^:\/\?]+)(:(?<port>\d+))?') {
                $dbHost = $matches['host']
                if ($matches['port']) { $dbPort = [int]$matches['port'] } else { $dbPort = 5432 }
            } else {
                Write-Host "[WARN] DB_URL no coincide con el patrón esperado: $dbUrl. Usando localhost:5432" -ForegroundColor Yellow
                $dbHost = 'localhost'
                $dbPort = 5432
            }
        }

        Write-Host "[INFO] Comprobando conectividad a la BD Insforge en $dbHost`:$dbPort (espera máxima $InsforgeWaitSeconds s)" -ForegroundColor Cyan
        $deadline = (Get-Date).AddSeconds($InsforgeWaitSeconds)
        $ok = $false
        while ((Get-Date) -lt $deadline) {
            $conn = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
            if ($conn -and $conn.TcpTestSucceeded) {
                Write-Host "[INFO] BD alcanzable en $dbHost`:$dbPort" -ForegroundColor Green
                $ok = $true
                break
            }
            Write-Host "[INFO] BD no disponible aún en $dbHost`:$dbPort. Reintentando en 5s..." -ForegroundColor DarkYellow
            Start-Sleep -Seconds 5
        }
        if (-not $ok) {
            throw "Timeout esperando conexión a BD Insforge en $dbHost`:$dbPort después de $InsforgeWaitSeconds segundos"
        }
    } catch {
        throw $_
    }
}

foreach ($service in $services) {
    Start-ServiceWindow -Service $service
    Start-Sleep -Seconds 3

    if ($service.Name -eq 'eureka-server') {
        Wait-ForHttp -Url $service.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $service.Name
        Start-Sleep -Seconds 5
        continue
    }

    Wait-ForHttp -Url $service.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $service.Name
}

if ($RunSmokeTest) {
    Write-Host "=== Ejecutando smoke test E2E ===" -ForegroundColor Cyan
    & (Join-Path $root 'scripts\smoke-test-e2e.ps1')
}

Write-Host "=== Todo levantado correctamente ===" -ForegroundColor Green
Write-Host "Gateway: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "Eureka : http://localhost:8761" -ForegroundColor Cyan

