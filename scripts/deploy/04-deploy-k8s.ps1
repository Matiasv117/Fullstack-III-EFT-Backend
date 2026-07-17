# Deploy de todos los servicios a EKS
# Reemplaza ACCOUNT_ID con tu AWS Account ID

param(
    [Parameter(Mandatory=$true)]
    [string]$AccountId
)

$Region = "us-east-1"
$ECR_BASE = "$AccountId.dkr.ecr.$Region.amazonaws.com"
$K8S_DIR = "C:\Users\ibane\Desktop\Fullstack\Fullstack-III-EFT-Backend\k8s"

Write-Host "=== Actualizando imagenes en manifests ===" -ForegroundColor Cyan

# Reemplazar <ACCOUNT_ID> en todos los manifests
Get-ChildItem "$K8S_DIR\*.yml" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace "<ACCOUNT_ID>", $AccountId
    Set-Content $_.FullName -Value $content -NoNewline
    Write-Host "Updated: $($_.Name)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Desplegando namespace + secrets ===" -ForegroundColor Cyan
kubectl apply -f "$K8S_DIR\namespace.yml"
kubectl apply -f "$K8S_DIR\secrets.yml"

Write-Host ""
Write-Host "=== Desplegando microservicios ===" -ForegroundColor Cyan
kubectl apply -f "$K8S_DIR\ms-notificaciones.yml"
kubectl apply -f "$K8S_DIR\ms-gestionpacientes.yml"
kubectl apply -f "$K8S_DIR\ms-optimizacion.yml"
kubectl apply -f "$K8S_DIR\ms-auth.yml"

Write-Host ""
Write-Host "=== Desplegando API Gateway ===" -ForegroundColor Cyan
kubectl apply -f "$K8S_DIR\api-gateway.yml"

Write-Host ""
Write-Host "=== Desplegando Frontend ===" -ForegroundColor Cyan
kubectl apply -f "$K8S_DIR\frontend.yml"

Write-Host ""
Write-Host "=== Esperando pods... ===" -ForegroundColor Yellow
kubectl get pods -n insforge -w

Write-Host ""
Write-Host "=== Services ===" -ForegroundColor Cyan
kubectl get svc -n insforge
