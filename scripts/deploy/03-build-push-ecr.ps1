# Crear repos ECR + build + push de todas las imagenes
# Reemplaza ACCOUNT_ID con tu AWS Account ID

param(
    [Parameter(Mandatory=$true)]
    [string]$AccountId
)

$Region = "us-east-1"
$ECR_BASE = "$AccountId.dkr.ecr.$Region.amazonaws.com"

Write-Host "=== Creando repositorios ECR ===" -ForegroundColor Cyan

$repos = @(
    "api-gateway",
    "ms-auth",
    "ms-gestionpacientes",
    "ms-optimizacion",
    "ms-notificaciones",
    "frontend"
)

foreach ($repo in $repos) {
    Write-Host "Creando repo: $repo" -ForegroundColor Yellow
    aws ecr create-repository --repository-name $repo --region $Region 2>$null
}

Write-Host ""
Write-Host "=== Login a ECR ===" -ForegroundColor Cyan
aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin $ECR_BASE

Write-Host ""
Write-Host "=== Building y pushing imagenes ===" -ForegroundColor Cyan

# Backend base path
$BACKEND = "C:\Users\ibane\Desktop\Fullstack\Fullstack-III-EFT-Backend"

$images = @(
    @{ name = "api-gateway";       path = "$BACKEND\api-gateway";       port = 8080 },
    @{ name = "ms-auth";           path = "$BACKEND\ms-auth";           port = 8087 },
    @{ name = "ms-gestionpacientes"; path = "$BACKEND\ms-gestionpacientes"; port = 8083 },
    @{ name = "ms-optimizacion";   path = "$BACKEND\ms-optimizacion";   port = 8084 },
    @{ name = "ms-notificaciones"; path = "$BACKEND\ms-notificaciones"; port = 8085 },
    @{ name = "frontend";          path = "$BACKEND\Fullstack-III-EFT-Frontend"; port = 80 }
)

foreach ($img in $images) {
    Write-Host ""
    Write-Host "--- Building: $($img.name) ---" -ForegroundColor Yellow

    # Build
    Push-Location $img.path
    docker build -t "$($img.name):latest" .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR building $($img.name)" -ForegroundColor Red
        Pop-Location
        continue
    }

    # Tag
    docker tag "$($img.name):latest" "$ECR_BASE/$($img.name):latest"

    # Push
    Write-Host "Pushing: $($img.name)" -ForegroundColor Yellow
    docker push "$ECR_BASE/$($img.name):latest"
    Pop-Location
}

Write-Host ""
Write-Host "=== Todas las imagenes pushed ===" -ForegroundColor Green
Write-Host "Repos en ECR:" -ForegroundColor Cyan
aws ecr describe-repositories --region $Region --query "repositories[].repositoryName" --output table
