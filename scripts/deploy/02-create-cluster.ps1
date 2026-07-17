# Verificar credenciales AWS antes de crear cluster
# Ejecutar primero para asegurar que las creds funcionan

Write-Host "=== Verificando credenciales AWS ===" -ForegroundColor Cyan
aws sts get-caller-identity

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Credenciales AWS invalidas. Ejecuta: aws configure" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Credenciales OK. Creando cluster EKS..." -ForegroundColor Green
Write-Host "Esto tarda ~15-20 minutos. Ve a tomar un cafe." -ForegroundColor Yellow
Write-Host ""

# Crear cluster con VPC completa
eksctl create cluster `
    --name insforge-eks `
    --region us-east-1 `
    --nodegroup-name default `
    --node-type t3.small `
    --nodes 2 `
    --nodes-min 1 `
    --nodes-max 3 `
    --managed

# Actualizar kubeconfig
aws eks update-kubeconfig --region us-east-1 --name insforge-eks

Write-Host ""
Write-Host "=== Cluster listo ===" -ForegroundColor Green
kubectl get nodes
