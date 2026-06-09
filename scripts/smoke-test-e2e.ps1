param(
    [string]$GatewayBaseUrl = "http://localhost:8080",
    [string]$GestionUrl = "http://localhost:8083",
    [string]$NotificacionesUrl = "http://localhost:8085",
    [string]$OptimizacionUrl = "http://localhost:8084"
)

$ErrorActionPreference = "Stop"

function Test-Status {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [int]$ExpectedStatus = 200,
        [string]$Body = "",
        [string]$ContentType = "application/json"
    )

    try {
        if ($Body -ne "") {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -Body $Body -ContentType $ContentType -UseBasicParsing -TimeoutSec 15
        } else {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -UseBasicParsing -TimeoutSec 15
        }
        $status = [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $status = [int]$_.Exception.Response.StatusCode
        } else {
            Write-Host "[FAIL] $Method $Url -> sin respuesta HTTP" -ForegroundColor Red
            throw
        }
    }

    if ($status -ne $ExpectedStatus) {
        Write-Host "[FAIL] $Method $Url -> esperado $ExpectedStatus, obtenido $status" -ForegroundColor Red
        exit 1
    }

    Write-Host "[OK]   $Method $Url -> $status" -ForegroundColor Green
}

Write-Host "=== Smoke Test E2E Fase 1.1 ==="

# Health/readiness de los 3 microservicios
Test-Status -Url "$GestionUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$GestionUrl/actuator/health/readiness" -ExpectedStatus 200

Test-Status -Url "$NotificacionesUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$NotificacionesUrl/actuator/health/readiness" -ExpectedStatus 200

Test-Status -Url "$OptimizacionUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$OptimizacionUrl/actuator/health/readiness" -ExpectedStatus 200


# 404: recurso inexistente
Test-Status -Url "$GatewayBaseUrl/pacientes/99999999" -ExpectedStatus 404

# 409: conflicto por DNI duplicado
$dni = "SMOKE-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$pacienteJson = @"
{
  "nombre": "Smoke",
  "apellido": "Test",
  "dni": "$dni",
  "telefono": "111111",
  "email": "smoke@test.local"
}
"@
Test-Status -Url "$GatewayBaseUrl/pacientes" -Method POST -Body $pacienteJson -ExpectedStatus 200
Test-Status -Url "$GatewayBaseUrl/pacientes" -Method POST -Body $pacienteJson -ExpectedStatus 409

# 400: validacion basica de notificaciones (pacienteId invalido)
$notifJson = @"
{
  "pacienteId": 0,
  "tipo": "PACIENTE_ASIGNADO",
  "mensaje": "abc"
}
"@
Test-Status -Url "$GatewayBaseUrl/api/notificaciones" -Method POST -Body $notifJson -ExpectedStatus 400

Write-Host "=== Smoke test completado correctamente ===" -ForegroundColor Cyan

