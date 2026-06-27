param(
    [switch]$RestartExisting,
    [switch]$RunSmokeTest,
    [int]$StartupTimeoutSeconds = 240
)

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

$services = @(
    [ordered]@{ Name = 'eureka-server';      Path = Join-Path $root 'eureka-server';         Port = 8761; Ready = 'Http'; Url = 'http://localhost:8761';                         Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-auth';            Path = Join-Path $root 'ms-auth';               Port = 8087; Ready = 'Http'; Url = 'http://localhost:8087/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-auditoria';       Path = Join-Path $root 'ms-auditoria';          Port = 8088; Ready = 'Http'; Url = 'http://localhost:8088/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-gestionpacientes'; Path = Join-Path $root 'ms-gestionpacientes';  Port = 8083; Ready = 'Http'; Url = 'http://localhost:8083/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-notificaciones';   Path = Join-Path $root 'ms-notificaciones';    Port = 8085; Ready = 'Http'; Url = 'http://localhost:8085/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-optimizacion';     Path = Join-Path $root 'ms-optimizacion';      Port = 8084; Ready = 'Http'; Url = 'http://localhost:8084/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'ms-progreso';          Path = Join-Path $root 'ms-progreso';           Port = 8086; Ready = 'Http'; Url = 'http://localhost:8086/actuator/health';          Command = '.\mvnw.cmd spring-boot:run' },
    [ordered]@{ Name = 'api-gateway';         Path = Join-Path $root 'api-gateway';          Port = 8080; Ready = 'Port'; Url = $null;                                           Command = '.\mvnw.cmd spring-boot:run' },
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

function Wait-ForPort {
    param(
        [int]$Port,
        [int]$TimeoutSeconds,
        [string]$ServiceName,
        [System.Diagnostics.Process]$Process
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        if ($null -ne $Process -and $Process.HasExited) {
            throw "$ServiceName finalizo durante el arranque (exit code: $($Process.ExitCode)). Revisa la ventana/log de ese servicio."
        }

        if (Test-PortListening -Port $Port) {
            Write-Host "[READY] $ServiceName -> Puerto $Port" -ForegroundColor Green
            return
        }

        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)

    throw "Timeout esperando $ServiceName en el puerto $Port"
}

function Wait-ForHttp {
    param(
        [string]$Url,
        [int]$TimeoutSeconds,
        [string]$ServiceName,
        [System.Diagnostics.Process]$Process
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        if ($null -ne $Process -and $Process.HasExited) {
            throw "$ServiceName finalizo durante el arranque (exit code: $($Process.ExitCode)). Revisa la ventana/log de ese servicio."
        }

        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 30
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                Write-Host "[READY] $ServiceName -> $Url ($($response.StatusCode))" -ForegroundColor Green
                return
            }
        } catch {
            Start-Sleep -Seconds 5
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
        return $null
    }

    Write-Host "[START] $($Service.Name)" -ForegroundColor Cyan
    
    # Si existe local-insforge.env, leer variables y pasarlas explícitamente
    $envFile = Join-Path $root 'config\local-insforge.env'
    $startCmd = $Service.Command
    
    if (Test-Path $envFile) {
        # Leer variables del archivo
        $springProfiles = ""
        $dbUrl = ""
        $dbUsername = ""
        $dbPassword = ""
        
        Get-Content $envFile | ForEach-Object {
            $line = $_.Trim()
            if ($line -eq '' -or $line.StartsWith('#')) { return }
            if ($line -match '^([^#=]+)=(.*)$') {
                $name = $matches[1].Trim()
                $val = $matches[2].Trim()
                
                switch ($name) {
                    "SPRING_PROFILES_ACTIVE" { $springProfiles = $val }
                    "DB_URL" { $dbUrl = $val }
                    "DB_USERNAME" { $dbUsername = $val }
                    "DB_PASSWORD" { $dbPassword = $val }
                }
            }
        }
        
        # Construir comando con variables de entorno explícitas
        if ($springProfiles -and $dbUrl -and $dbUsername -and $dbPassword) {
            $startCmd = "`$env:SPRING_PROFILES_ACTIVE='$springProfiles'; `$env:DB_URL='$dbUrl'; `$env:DB_USERNAME='$dbUsername'; `$env:DB_PASSWORD='$dbPassword'; " + $Service.Command
        }
    }
    
    return Start-Process -FilePath powershell.exe `
        -WorkingDirectory $Service.Path `
        -ArgumentList @('-NoLogo', '-NoProfile', '-NoExit', '-Command', $startCmd) `
        -PassThru
}

Write-Host "=== Levantando todos los servicios ===" -ForegroundColor Cyan
Write-Host "Raiz: $root" -ForegroundColor DarkGray

# Iniciar RabbitMQ y Redis con Docker Compose
Write-Host "[START] Iniciando RabbitMQ y Redis con Docker Compose..." -ForegroundColor Cyan
$dockerComposePath = Join-Path $root 'docker-compose.yml'
if (Test-Path $dockerComposePath) {
    docker-compose -f $dockerComposePath up -d rabbitmq redis
    Write-Host "[WAIT] Esperando a que RabbitMQ y Redis estén listos..." -ForegroundColor Cyan
    Start-Sleep -Seconds 15
} else {
    Write-Host "[WARN] No se encontró docker-compose.yml en $root" -ForegroundColor Yellow
}

# 1) Arrancar Eureka primero (obligatorio, todos dependen de él)
$eurekaSvc = $services | Where-Object { $_.Name -eq 'eureka-server' }
$eurekaProcess = Start-ServiceWindow -Service $eurekaSvc
Start-Sleep -Seconds 10
if ($null -ne $eurekaProcess -and $eurekaProcess.HasExited) {
    throw "eureka-server termino inmediatamente tras iniciar (exit code: $($eurekaProcess.ExitCode))."
}
Wait-ForHttp -Url $eurekaSvc.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $eurekaSvc.Name -Process $eurekaProcess
Wait-ForHttp -Url 'http://localhost:8761/eureka/apps' -TimeoutSeconds 60 -ServiceName 'eureka-registry' -Process $eurekaProcess
Start-Sleep -Seconds 5

# 2) Arrancar el resto de servicios en paralelo
$otherServices = $services | Where-Object { $_.Name -ne 'eureka-server' }
$launched = @{}
foreach ($service in $otherServices) {
    $proc = Start-ServiceWindow -Service $service
    if ($proc) {
        $launched[$service.Name] = @{ Process = $proc; Service = $service }
    }
}

# 3) Pequeña pausa para que los procesos se estabilicen antes de verificar
Start-Sleep -Seconds 5

# 4) Verificar que ningún proceso haya muerto inmediatamente
foreach ($entry in $launched.GetEnumerator()) {
    $name = $entry.Key
    $proc = $entry.Value.Process
    if ($proc.HasExited) {
        throw "$name termino inmediatamente tras iniciar (exit code: $($proc.ExitCode))."
    }
}

# 5) Esperar por cada servicio (health checks rápidos, ya están corriendo en paralelo)
foreach ($entry in $launched.GetEnumerator()) {
    $name = $entry.Key
    $proc = $entry.Value.Process
    $svc = $entry.Value.Service

    if ($svc.Ready -eq 'Port') {
        Wait-ForPort -Port $svc.Port -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $name -Process $proc
    } else {
        Wait-ForHttp -Url $svc.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $name -Process $proc
    }
}

if ($RunSmokeTest) {
    Write-Host "=== Ejecutando smoke test E2E ===" -ForegroundColor Cyan
    & (Join-Path $root 'scripts\smoke-test-e2e.ps1')
}

Write-Host "=== Todo levantado correctamente ===" -ForegroundColor Green
Write-Host "Gateway: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Eureka : http://localhost:8761" -ForegroundColor Cyan
