<#
.SYNOPSIS
    Smoke test for ECS deployment.

.DESCRIPTION
    Validates all services are healthy and communication works.

    Run from repo root:
    .\aws\05-smoke-test.ps1
#>
param(
    [string]$Region = "us-east-1",
    [string]$ClusterName = "insforge-cluster"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Load config
$config = @{}
Get-Content "$ScriptDir\config.env" | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $config[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$ALB_DNS = $config.ALB_DNS
$BaseUrl = "http://$ALB_DNS"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " ECS Deployment Smoke Test" -ForegroundColor Cyan
Write-Host " ALB: $ALB_DNS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passed = 0
$failed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [string]$Method = "GET",
        [int]$ExpectedStatus = 200,
        [string]$Body = ""
    )

    try {
        $params = @{
            Uri = $Url
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 30
        }
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = "application/json"
        }

        $response = Invoke-WebRequest @params
        $status = [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
        } else {
            $status = 0
        }
    }

    if ($status -eq $ExpectedStatus) {
        Write-Host "[PASS] $Name -> $status" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host "[FAIL] $Name -> expected $ExpectedStatus, got $status" -ForegroundColor Red
        $script:failed++
    }
}

# ─── ECS CLUSTER STATUS ──────────────────────────
Write-Host "`n--- ECS Cluster Status ---" -ForegroundColor Yellow

$services = aws ecs list-services --cluster $ClusterName --region $Region --query 'serviceArns' --output text
$serviceCount = ($services -split "`t").Count
Write-Host "Services in cluster: $serviceCount" -ForegroundColor Cyan

foreach ($svcArn in ($services -split "`t")) {
    if (-not $svcArn) { continue }
    $svcName = $svcArn -replace ".*/", ""
    $details = aws ecs describe-services --cluster $ClusterName --services $svcName --region $Region | ConvertFrom-Json
    $svc = $details.services[0]
    $running = $svc.runningCount
    $desired = $svc.desiredCount
    $status = $svc.status
    
    if ($running -eq $desired -and $status -eq "ACTIVE") {
        Write-Host "  [OK] $svcName: $running/$desired tasks (ACTIVE)" -ForegroundColor Green
    } else {
        Write-Host "  [!!] $svcName: $running/$desired tasks ($status)" -ForegroundColor Yellow
    }
}

# ─── HTTP HEALTH CHECKS ──────────────────────────
Write-Host "`n--- HTTP Health Checks ---" -ForegroundColor Yellow

Start-Sleep -Seconds 10

Test-Endpoint -Name "Frontend" -Url "$BaseUrl/" -ExpectedStatus 200
Test-Endpoint -Name "API Gateway" -Url "$BaseUrl/api/actuator/health" -ExpectedStatus 200
Test-Endpoint -Name "BFF" -Url "$BaseUrl/api/portal/" -ExpectedStatus 200

# ─── AUTH FLOW ───────────────────────────────────
Write-Host "`n--- Auth Flow ---" -ForegroundColor Yellow

try {
    $loginResp = Invoke-WebRequest -Uri "$BaseUrl/api/auth/login" -Method POST `
        -Body '{"username":"admin","password":"admin123"}' `
        -ContentType "application/json" -UseBasicParsing -TimeoutSec 30
    
    $loginData = $loginResp.Content | ConvertFrom-Json
    if ($loginData.token) {
        Write-Host "[PASS] Login -> JWT token received" -ForegroundColor Green
        $script:passed++
        
        $headers = @{ "Authorization" = "Bearer $($loginData.token)" }
        
        # Test authenticated endpoint
        try {
            $pacResp = Invoke-WebRequest -Uri "$BaseUrl/api/pacientes" -Headers $headers -UseBasicParsing -TimeoutSec 30
            Write-Host "[PASS] GET /api/pacientes -> $($pacResp.StatusCode)" -ForegroundColor Green
            $script:passed++
        } catch {
            Write-Host "[FAIL] GET /api/pacientes -> $($_.Exception.Response.StatusCode)" -ForegroundColor Red
            $script:failed++
        }
    } else {
        Write-Host "[FAIL] Login -> no token in response" -ForegroundColor Red
        $script:failed++
    }
} catch {
    Write-Host "[FAIL] Login -> $($_.Exception.Message)" -ForegroundColor Red
    $script:failed++
}

# ─── RABBITMQ MANAGEMENT ─────────────────────────
Write-Host "`n--- RabbitMQ Management ---" -ForegroundColor Yellow

try {
    $rmqResp = Invoke-WebRequest -Uri "http://$($config.ALB_DNS):15672" -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
    Write-Host "[PASS] RabbitMQ Management UI accessible" -ForegroundColor Green
    $script:passed++
} catch {
    Write-Host "[INFO] RabbitMQ Management not exposed via ALB (expected - internal only)" -ForegroundColor DarkYellow
}

# ─── CLOUDWATCH LOGS ─────────────────────────────
Write-Host "`n--- CloudWatch Logs ---" -ForegroundColor Yellow

$logGroups = aws logs describe-log-groups --log-group-name-prefix "/ecs" --region $Region --query 'logGroups[].logGroupName' --output text
$logCount = ($logGroups -split "`t").Count
Write-Host "Log groups found: $logCount" -ForegroundColor Cyan

foreach ($lg in ($logGroups -split "`t")) {
    if (-not $lg) { continue }
    $streams = aws logs describe-log-streams --log-group-name $lg --order-by LastEventTime --descending --limit 1 --region $Region --query 'logStreams[].lastEventTime' --output text
    if ($streams) {
        Write-Host "  [OK] $lg (has logs)" -ForegroundColor Green
    } else {
        Write-Host "  [..] $lg (no logs yet)" -ForegroundColor DarkYellow
    }
}

# ─── SUMMARY ─────────────────────────────────────
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " Results: $passed passed, $failed failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "========================================" -ForegroundColor Cyan

if ($failed -gt 0) {
    Write-Host "`nSome checks failed. Check the logs:" -ForegroundColor Yellow
    Write-Host "  aws logs tail /ecs/api-gateway --follow --region $Region" -ForegroundColor DarkGray
    exit 1
}
