<#
.SYNOPSIS
    Pushes all local Docker images to Amazon ECR.

.DESCRIPTION
    Prerequisites:
    - AWS CLI configured (aws configure)
    - Docker images built (run build-all-images.ps1 first)
    - ECR repositories created (via console or 01-setup-infra.ps1)

    Usage:
    .\aws\push-to-ecr.ps1
#>
param(
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"

$AccountId = aws sts get-caller-identity --query Account --output text
if (-not $AccountId) {
    Write-Host "ERROR: AWS CLI not configured. Run 'aws configure' first." -ForegroundColor Red
    exit 1
}

$Registry = "$AccountId.dkr.ecr.$Region.amazonaws.com"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Pushing Images to ECR" -ForegroundColor Cyan
Write-Host " Registry: $Registry" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Login to ECR
Write-Host "`nLogging in to ECR..." -ForegroundColor Yellow
aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin $Registry
if ($LASTEXITCODE -ne 0) {
    Write-Host "ECR login failed" -ForegroundColor Red
    exit 1
}
Write-Host "Login OK" -ForegroundColor Green

# Images to push
$images = @(
    "eureka-server",
    "api-gateway",
    "bff",
    "ms-auth",
    "ms-gestionpacientes",
    "ms-optimizacion",
    "ms-notificaciones",
    "ms-progreso",
    "ms-auditoria",
    "frontend"
)

foreach ($img in $images) {
    Write-Host "`n[$img] Pushing..." -ForegroundColor Yellow
    
    # Check if local image exists
    $local = docker images "$($img):latest" --format "{{.Repository}}"
    if (-not $local) {
        Write-Host "  [SKIP] No local image for $img" -ForegroundColor DarkYellow
        continue
    }
    
    # Tag
    docker tag "$($img):latest" "$Registry/$($img):latest"
    
    # Push
    docker push "$Registry/$($img):latest"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Pushed $Registry/$($img):latest" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Push failed for $img" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " All images pushed to ECR!" -ForegroundColor Green
Write-Host " Next: Create ECS Services via console" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
