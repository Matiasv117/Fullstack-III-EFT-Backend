# Eliminar cluster EKS (para ahorrar dinero cuando no se necesita)

param(
    [switch]$Confirm
)

if (-not $Confirm) {
    Write-Host "Esto eliminara el cluster EKS y todos los recursos asociados." -ForegroundColor Red
    Write-Host "Usa -Confirm para ejecutar." -ForegroundColor Yellow
    exit 0
}

Write-Host "=== Eliminando cluster insforge-eks ===" -ForegroundColor Red
eksctl delete cluster --name insforge-eks --region us-east-1

Write-Host ""
Write-Host "=== Cluster eliminado ===" -ForegroundColor Green
Write-Host "Recuerda tambien eliminar los repos ECR si no los necesitas:" -ForegroundColor Yellow
Write-Host "  aws ecr delete-repository --repository-name <repo-name> --region us-east-1 --force" -ForegroundColor Gray
