<#
.SYNOPSIS
    Builds all Docker images locally for ECS deployment.

.DESCRIPTION
    Prerequisites:
    - Docker Desktop running
    - Java 17+ and Maven installed

    Usage:
    .\aws\build-all-images.ps1
#>

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path | Split-Path -Parent

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Building All Docker Images" -ForegroundColor Cyan
"========================================" -ForegroundColor Cyan

# Verify Docker is running
try {
    docker info 2>$null | Out-Null
} catch {
    Write-Host "ERROR: Docker Desktop is not running. Start it first." -ForegroundColor Red
    exit 1
}

$services = @(
    @{ Name = "eureka-server"; Port = 8761 },
    @{ Name = "api-gateway"; Port = 8080 },
    @{ Name = "bff"; Port = 8097 },
    @{ Name = "ms-auth"; Port = 8087 },
    @{ Name = "ms-gestionpacientes"; Port = 8083 },
    @{ Name = "ms-optimizacion"; Port = 8084 },
    @{ Name = "ms-notificaciones"; Port = 8085 },
    @{ Name = "ms-progreso"; Port = 8086 },
    @{ Name = "ms-auditoria"; Port = 8088 }
)

# Build backend services
foreach ($svc in $services) {
    Write-Host "`n[$($svc.Name)] Building..." -ForegroundColor Yellow
    Push-Location "$Root\$($svc.Name)"
    
    # Build with Maven
    Write-Host "  Maven package..." -ForegroundColor DarkGray
    mvn clean package -DskipTests -q 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  Maven FAILED for $($svc.Name)" -ForegroundColor Red
        Pop-Location
        continue
    }
    
    # Build Docker image
    Write-Host "  Docker build..." -ForegroundColor DarkGray
    docker build -t "$($svc.Name):latest" . 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] $($svc.Name):latest" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] $($svc.Name)" -ForegroundColor Red
    }
    
    Pop-Location
}

# Build frontend
Write-Host "`n[frontend] Building..." -ForegroundColor Yellow
Push-Location "$Root\Fullstack-III-EFT-Frontend"

Write-Host "  npm install + build..." -ForegroundColor DarkGray
npm ci --silent 2>$null
npm run build 2>$null

Write-Host "  Docker build..." -ForegroundColor DarkGray
docker build -t "frontend:latest" . 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] frontend:latest" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] frontend" -ForegroundColor Red
}

Pop-Location

# List all images
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " Built Images:" -ForegroundColor Cyan
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | Select-String "insforge|ms-|api-gateway|bff|eureka|frontend"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`nNext: Push to ECR (see aws\push-to-ecr.ps1)" -ForegroundColor Cyan
