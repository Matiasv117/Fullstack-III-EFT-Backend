param(
    [int[]]$Ports = @(8761, 8080, 8088, 8087, 8083, 8084, 8085, 8086, 8097)
)

$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

# Detener RabbitMQ y Redis con Docker Compose
Write-Host "[STOP] Deteniendo RabbitMQ y Redis con Docker Compose..." -ForegroundColor Cyan
$dockerComposePath = Join-Path $root 'docker-compose.yml'
if (Test-Path $dockerComposePath) {
    docker-compose -f $dockerComposePath down
    Write-Host "[STOP] RabbitMQ y Redis detenidos" -ForegroundColor Green
} else {
    Write-Host "[WARN] No se encontró docker-compose.yml en $root" -ForegroundColor Yellow
}

foreach ($port in $Ports) {
    $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        try {
            Stop-Process -Id $listener.OwningProcess -Force -ErrorAction Stop
            Write-Host "[STOP] Puerto $port -> PID $($listener.OwningProcess)" -ForegroundColor DarkYellow
        } catch {
            Write-Host "[WARN] No se pudo detener el proceso en el puerto $port" -ForegroundColor Yellow
        }
    }
}

Write-Host "=== Servicios detenidos ===" -ForegroundColor Green

