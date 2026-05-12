param(
    [int[]]$Ports = @(8761, 8080, 8083, 8084, 8085, 8097)
)

$ErrorActionPreference = 'Stop'

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

