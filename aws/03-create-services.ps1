<#
.SYNOPSIS
    Creates all ECS Services with proper networking and load balancing.

.DESCRIPTION
    Prerequisites:
    - Infrastructure created via 01-setup-infra.ps1
    - Task Definitions and ALB created via 02-register-tasks-and-alb.ps1
    - Docker images pushed to ECR

    Run from repo root:
    .\aws\03-create-services.ps1
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

$PRIV_A = $config.SUBNET_PRIVATE_A
$PRIV_B = $config.SUBNET_PRIVATE_B
$SG_BE = $config.SG_BACKEND
$SG_FE = $config.SG_FRONTEND
$SG_INFRA = $config.SG_INFRA

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Creating ECS Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

function New-InsForgeService {
    param(
        [string]$ServiceName,
        [string]$TaskFamily,
        [int]$DesiredCount = 1,
        [string[]]$Subnets,
        [string]$SecurityGroup,
        [string]$TargetGroupArn = $null,
        [int]$HealthCheckGraceSeconds = 0
    )

    $existing = aws ecs describe-services --cluster $ClusterName --services $ServiceName --region $Region 2>$null
    $svcCount = ($existing | ConvertFrom-Json).services.Count
    if ($svcCount -gt 0) {
        $state = ($existing | ConvertFrom-Json).services[0].status
        if ($state -eq "ACTIVE") {
            Write-Host "  [SKIP] $ServiceName already exists and active" -ForegroundColor Yellow
            return
        }
    }

    $networkConfig = "awsvpcConfiguration={subnets=[$($Subnets -join ',')],securityGroups=[$SecurityGroup],assignPublicIp=DISABLED}"
    
    $cmd = @(
        "aws", "ecs", "create-service",
        "--cluster", $ClusterName,
        "--service-name", $ServiceName,
        "--task-definition", $TaskFamily,
        "--desired-count", $DesiredCount,
        "--launch-type", "FARGATE",
        "--network-configuration", $networkConfig,
        "--region", $Region
    )

    if ($TargetGroupArn) {
        $cmd += "--load-balancers"
        $cmd += "targetGroupArn=$TargetGroupArn,containerName=$ServiceName,containerPort=0"
    }

    if ($HealthCheckGraceSeconds -gt 0) {
        $cmd += "--health-check-grace-period-seconds"
        $cmd += $HealthCheckGraceSeconds
    }

    & $cmd[0] $cmd[1..($cmd.Length-1)] | Out-Null
    Write-Host "  [OK] $ServiceName (tasks: $DesiredCount)" -ForegroundColor Green
}

# ─── INFRASTRUCTURE SERVICES (Redis, RabbitMQ) ───
Write-Host "`n[1/3] Infrastructure Services..." -ForegroundColor Yellow

New-InsForgeService -ServiceName "redis" -TaskFamily "redis" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_INFRA

New-InsForgeService -ServiceName "rabbitmq" -TaskFamily "rabbitmq" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_INFRA

# ─── EUREKA SERVER (must start first) ────────────
Write-Host "`n[2/3] Eureka Server..." -ForegroundColor Yellow

New-InsForgeService -ServiceName "eureka-server" -TaskFamily "eureka-server" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE

Write-Host "  Waiting 60s for Eureka to start..." -ForegroundColor DarkYellow
Start-Sleep -Seconds 60

# ─── BACKEND SERVICES ────────────────────────────
Write-Host "`n[3/3] Backend Services..." -ForegroundColor Yellow

New-InsForgeService -ServiceName "api-gateway" -TaskFamily "api-gateway" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_GATEWAY -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "bff" -TaskFamily "bff" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_BFF -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-auth" -TaskFamily "ms-auth" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_AUTH -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-gestionpacientes" -TaskFamily "ms-gestionpacientes" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_GP -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-optimizacion" -TaskFamily "ms-optimizacion" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_OPT -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-notificaciones" -TaskFamily "ms-notificaciones" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_NOTIF -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-progreso" -TaskFamily "ms-progreso" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_PROG -HealthCheckGraceSeconds 60

New-InsForgeService -ServiceName "ms-auditoria" -TaskFamily "ms-auditoria" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_BE `
    -TargetGroupArn $config.TG_AUD -HealthCheckGraceSeconds 60

# ─── FRONTEND ────────────────────────────────────
Write-Host "`nFrontend Service..." -ForegroundColor Yellow

New-InsForgeService -ServiceName "frontend" -TaskFamily "frontend" -DesiredCount 1 `
    -Subnets @($PRIV_A, $PRIV_B) -SecurityGroup $SG_FE `
    -TargetGroupArn $config.TG_FRONTEND -HealthCheckGraceSeconds 30

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " All ECS Services created!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNext step: Run .\aws\04-autoscaling.ps1" -ForegroundColor Cyan
