<#
.SYNOPSIS
    Master deployment script — runs all steps in order.

.DESCRIPTION
    Executes the full InsForge ECS Fargate deployment:
    1. AWS infrastructure (VPC, ECS, ALB)
    2. Task Definitions + ALB routing
    3. ECS Services
    4. Auto Scaling
    5. Smoke Test

    Prerequisites:
    - AWS CLI installed and configured (aws configure)
    - PowerShell 5.1+

    Usage:
    .\aws\deploy.ps1                    # Full deployment
    .\aws\deploy.ps1 -SkipInfra         # Skip VPC/cluster (if already exists)
    .\aws\deploy.ps1 -OnlyInfra         # Only create infrastructure
#>
param(
    [string]$Region = "us-east-1",
    [string]$ClusterName = "insforge-cluster",
    [switch]$SkipInfra,
    [switch]$OnlyInfra,
    [switch]$SkipTest
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$StartTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " InsForge ECS Fargate Deployment" -ForegroundColor Cyan
Write-Host " Region: $Region" -ForegroundColor Cyan
Write-Host " Cluster: $ClusterName" -ForegroundColor Cyan
Write-Host " Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ─── STEP 1: INFRASTRUCTURE ──────────────────────
if (-not $SkipInfra) {
    Write-Host "`n>>> STEP 1: Creating AWS Infrastructure" -ForegroundColor Magenta
    & "$ScriptDir\01-setup-infra.ps1" -Region $Region -ClusterName $ClusterName
} else {
    Write-Host "`n>>> STEP 1: Skipped (infrastructure already exists)" -ForegroundColor Yellow
}

if ($OnlyInfra) {
    Write-Host "`nOnlyInfra flag set. Stopping here." -ForegroundColor Yellow
    exit 0
}

# ─── STEP 2: TASK DEFINITIONS + ALB ──────────────
Write-Host "`n>>> STEP 2: Registering Task Definitions & ALB" -ForegroundColor Magenta
& "$ScriptDir\02-register-tasks-and-alb.ps1" -Region $Region -ClusterName $ClusterName

# ─── STEP 3: ECS SERVICES ────────────────────────
Write-Host "`n>>> STEP 3: Creating ECS Services" -ForegroundColor Magenta
& "$ScriptDir\03-create-services.ps1" -Region $Region -ClusterName $ClusterName

# ─── STEP 4: AUTOSCALING ─────────────────────────
Write-Host "`n>>> STEP 4: Configuring Auto Scaling" -ForegroundColor Magenta
& "$ScriptDir\04-autoscaling.ps1" -Region $Region -ClusterName $ClusterName

# ─── STEP 5: SMOKE TEST ──────────────────────────
if (-not $SkipTest) {
    Write-Host "`n>>> STEP 5: Running Smoke Test" -ForegroundColor Magenta
    Write-Host "Waiting 120s for all services to stabilize..." -ForegroundColor DarkYellow
    Start-Sleep -Seconds 120
    & "$ScriptDir\05-smoke-test.ps1" -Region $Region -ClusterName $ClusterName
} else {
    Write-Host "`n>>> STEP 5: Skipped" -ForegroundColor Yellow
}

# ─── SUMMARY ─────────────────────────────────────
$Elapsed = (Get-Date) - $StartTime
Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Deployment complete!" -ForegroundColor Green
Write-Host " Total time: $($Elapsed.Minutes)m $($Elapsed.Seconds)s" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Load config for ALB DNS
if (Test-Path "$ScriptDir\config.env") {
    $config = @{}
    Get-Content "$ScriptDir\config.env" | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') {
            $config[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
    if ($config.ALB_DNS) {
        Write-Host "`nFrontend URL: http://$($config.ALB_DNS)" -ForegroundColor Cyan
        Write-Host "API Gateway:  http://$($config.ALB_DNS)/api/" -ForegroundColor Cyan
        Write-Host "Eureka:       http://$($config.ALB_DNS):8761" -ForegroundColor DarkYellow
    }
}
