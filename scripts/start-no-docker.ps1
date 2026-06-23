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
    
    # Usar el comando directamente sin variables de entorno personalizadas
    $startCmd = $Service.Command
    
    return Start-Process -FilePath powershell.exe `
        -WorkingDirectory $Service.Path `
        -ArgumentList @('-NoLogo', '-NoProfile', '-NoExit', '-Command', $startCmd) `
        -PassThru
}

Write-Host "=== Levantando todos los servicios (Sin Docker) ===" -ForegroundColor Cyan
Write-Host "Raiz: $root" -ForegroundColor DarkGray
Write-Host "[WARN] RabbitMQ y Redis no se iniciarán (requieren Docker)" -ForegroundColor Yellow
Write-Host "[INFO] Los servicios que dependen de RabbitMQ/Redis pueden fallar" -ForegroundColor Yellow
Write-Host ""

foreach ($service in $services) {
    $process = Start-ServiceWindow -Service $service
    Start-Sleep -Seconds 10

    if ($null -ne $process -and $process.HasExited) {
        throw "$($service.Name) termino inmediatamente tras iniciar (exit code: $($process.ExitCode))."
    }

    if ($service.Name -eq 'eureka-server') {
        Wait-ForHttp -Url $service.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $service.Name -Process $process
        # Evita carrera: el dashboard puede responder antes de que /eureka/apps acepte registros.
        Wait-ForHttp -Url 'http://localhost:8761/eureka/apps' -TimeoutSeconds 60 -ServiceName 'eureka-registry' -Process $process
        Start-Sleep -Seconds 5
        continue
    }

    if ($service.Ready -eq 'Port') {
        Wait-ForPort -Port $service.Port -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $service.Name -Process $process
        continue
    }

    Wait-ForHttp -Url $service.Url -TimeoutSeconds $StartupTimeoutSeconds -ServiceName $service.Name -Process $process
}

if ($RunSmokeTest) {
    Write-Host "=== Ejecutando smoke test E2E ===" -ForegroundColor Cyan
    & (Join-Path $root 'scripts\smoke-test-e2e.ps1')
}

Write-Host "=== Todo levantado correctamente ===" -ForegroundColor Green
Write-Host "Gateway: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Eureka : http://localhost:8761" -ForegroundColor Cyan
