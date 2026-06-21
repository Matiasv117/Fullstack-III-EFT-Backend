<#
.SYNOPSIS
    Smoke test E2E del sistema RedNorte via API Gateway.

.DESCRIPTION
    Valida health de microservicios, autenticacion, registro de paciente,
    eventos de auditoria (RabbitMQ) y casos de error HTTP.

    Prerrequisitos:
      1. docker compose up -d   (PostgreSQL, Redis, RabbitMQ)
      2. Eureka (8761), API Gateway (8080), BFF (8097) opcional
      3. MS con SPRING_PROFILES_ACTIVE=postgres:
         ms-auth (8087), ms-gestionpacientes (8083), ms-notificaciones (8085),
         ms-optimizacion (8084), ms-progreso (8086), ms-auditoria (8088)

.EXAMPLE
    .\scripts\smoke-test-e2e.ps1
    .\scripts\smoke-test-e2e.ps1 -SkipPrerequisites
#>
param(
    [string]$GatewayBaseUrl = "http://localhost:8080",
    [string]$AuthUrl = "http://localhost:8087",
    [string]$GestionUrl = "http://localhost:8083",
    [string]$NotificacionesUrl = "http://localhost:8085",
    [string]$OptimizacionUrl = "http://localhost:8084",
    [string]$AuditoriaUrl = "http://localhost:8088",
    [string]$ProgresoUrl = "http://localhost:8086",
    [switch]$SkipPrerequisites,
    [int]$AuditoriaWaitSeconds = 5
)

$ErrorActionPreference = "Stop"

function Test-Port {
    param([int]$Port)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("localhost", $Port)
        $tcp.Close()
        return $true
    } catch {
        return $false
    }
}

function Assert-Prerequisites {
    if ($SkipPrerequisites) {
        Write-Host "[WARN] Omitiendo verificacion de prerrequisitos (-SkipPrerequisites)" -ForegroundColor Yellow
        return
    }

    Write-Host "=== Verificando prerrequisitos ==="
    $required = @(
        @{ Name = "API Gateway"; Port = 8080 },
        @{ Name = "ms-gestionpacientes"; Port = 8083 },
        @{ Name = "ms-notificaciones"; Port = 8085 },
        @{ Name = "ms-optimizacion"; Port = 8084 },
        @{ Name = "ms-auth"; Port = 8087 },
        @{ Name = "ms-auditoria"; Port = 8088 }
    )
    $infra = @(
        @{ Name = "Redis"; Port = 6379 },
        @{ Name = "RabbitMQ"; Port = 5672 }
    )

    $failed = $false
    foreach ($item in ($required + $infra)) {
        if (Test-Port -Port $item.Port) {
            Write-Host "[OK]   $($item.Name) (puerto $($item.Port))" -ForegroundColor Green
        } else {
            Write-Host "[FAIL] $($item.Name) no responde en puerto $($item.Port)" -ForegroundColor Red
            $failed = $true
        }
    }

    if ($failed) {
        Write-Host ""
        Write-Host "Levanta la infraestructura antes de ejecutar:" -ForegroundColor Yellow
        Write-Host "  docker compose up -d" -ForegroundColor Yellow
        Write-Host "  # Luego Eureka, Gateway y los 6 microservicios con perfil postgres" -ForegroundColor Yellow
        exit 1
    }
}

function Invoke-Http {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [string]$Body = "",
        [string]$ContentType = "application/json",
        [hashtable]$Headers = @{}
    )

    if ($global:AuthHeaders -and $Url.StartsWith($GatewayBaseUrl)) {
        foreach ($key in $global:AuthHeaders.Keys) {
            if (-not $Headers.ContainsKey($key)) {
                $Headers[$key] = $global:AuthHeaders[$key]
            }
        }
    }

    $params = @{
        Uri = $Url
        Method = $Method
        UseBasicParsing = $true
        TimeoutSec = 15
    }
    if ($Body -ne "") {
        $params["Body"] = $Body
        $params["ContentType"] = $ContentType
    }
    if ($Headers.Count -gt 0) {
        $params["Headers"] = $Headers
    }

    return Invoke-WebRequest @params
}

function Test-Status {
    param(
        [string]$Url,
        [string]$Method = "GET",
        [int]$ExpectedStatus = 200,
        [string]$Body = "",
        [string]$ContentType = "application/json",
        [hashtable]$Headers = @{}
    )

    try {
        $response = Invoke-Http -Url $Url -Method $Method -Body $Body -ContentType $ContentType -Headers $Headers
        $status = [int]$response.StatusCode
        $script:lastResponse = $response
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $status = [int]$_.Exception.Response.StatusCode
            $script:lastResponse = $null
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

Assert-Prerequisites

# Autenticacion via API Gateway (token Base64 legacy compatible con filtros MS)
Write-Host "=== Autenticando con API Gateway ==="
try {
    $authResponse = Invoke-WebRequest -Uri "$GatewayBaseUrl/login/admin" -Method POST -UseBasicParsing -TimeoutSec 10
    $token = $authResponse.Content.Trim()
    $global:AuthHeaders = @{ "Authorization" = "Bearer $token" }
    Write-Host "[OK]   Token de administrador obtenido via Gateway" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] No se pudo obtener token del Gateway: $_" -ForegroundColor Red
    exit 1
}

# Login ms-auth (JWT real) via Gateway
Write-Host "=== Login ms-auth (JWT) ==="
try {
    $loginJson = '{"username":"admin","password":"admin123"}'
    $loginResp = Invoke-Http -Url "$GatewayBaseUrl/api/auth/login" -Method POST -Body $loginJson
    if ([int]$loginResp.StatusCode -eq 200) {
        $jwtBody = $loginResp.Content | ConvertFrom-Json
        Write-Host "[OK]   POST $GatewayBaseUrl/api/auth/login -> 200 (usuario: $($jwtBody.username))" -ForegroundColor Green
    }
} catch {
    Write-Host "[WARN] ms-auth no disponible via Gateway; continuando con token Gateway" -ForegroundColor Yellow
}

Write-Host "=== Health checks ==="
Test-Status -Url "$GestionUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$NotificacionesUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$OptimizacionUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$AuthUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$AuditoriaUrl/actuator/health" -ExpectedStatus 200
Test-Status -Url "$ProgresoUrl/actuator/health" -ExpectedStatus 200

Write-Host "=== Flujo principal: paciente + auditoria ==="
$dni = "SMOKE-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$pacienteJson = @"
{
  "nombre": "Smoke",
  "apellido": "Test",
  "dni": "$dni",
  "telefono": "111111111",
  "email": "smoke@test.local"
}
"@
Test-Status -Url "$GatewayBaseUrl/pacientes" -Method POST -Body $pacienteJson -ExpectedStatus 200
$paciente = $script:lastResponse.Content | ConvertFrom-Json
Write-Host "[OK]   Paciente registrado con ID $($paciente.id)" -ForegroundColor Green

Write-Host "=== Esperando evento de auditoria (RabbitMQ, ${AuditoriaWaitSeconds}s) ==="
Start-Sleep -Seconds $AuditoriaWaitSeconds
Test-Status -Url "$GatewayBaseUrl/api/auditoria/eventos/accion/PACIENTE_REGISTRADO" -ExpectedStatus 200
$eventos = $script:lastResponse.Content | ConvertFrom-Json
if ($eventos.Count -eq 0) {
    Write-Host "[FAIL] No se encontraron eventos PACIENTE_REGISTRADO en ms-auditoria" -ForegroundColor Red
    exit 1
}
Write-Host "[OK]   $($eventos.Count) evento(s) PACIENTE_REGISTRADO en auditoria" -ForegroundColor Green

Write-Host "=== Casos de error HTTP ==="
Test-Status -Url "$GatewayBaseUrl/pacientes/99999999" -ExpectedStatus 404
Test-Status -Url "$GatewayBaseUrl/pacientes" -Method POST -Body $pacienteJson -ExpectedStatus 409

$notifJson = '{"pacienteId":0,"tipo":"PACIENTE_ASIGNADO","mensaje":"abc"}'
Test-Status -Url "$GatewayBaseUrl/api/notificaciones" -Method POST -Body $notifJson -ExpectedStatus 400

Write-Host "=== Smoke test E2E completado correctamente ===" -ForegroundColor Cyan
