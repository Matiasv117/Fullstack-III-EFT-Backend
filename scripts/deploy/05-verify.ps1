# Verificar que todo esta funcionando en EKS

Write-Host "=== Verificando deploy ===" -ForegroundColor Cyan

Write-Host ""
Write-Host "--- Nodes ---" -ForegroundColor Yellow
kubectl get nodes

Write-Host ""
Write-Host "--- Pods ---" -ForegroundColor Yellow
kubectl get pods -n insforge

Write-Host ""
Write-Host "--- Services ---" -ForegroundColor Yellow
kubectl get svc -n insforge

Write-Host ""
Write-Host "--- Health Checks ---" -ForegroundColor Yellow

$services = @(
    @{ name = "ms-auth";           port = 8087 },
    @{ name = "ms-gestionpacientes"; port = 8083 },
    @{ name = "ms-optimizacion";   port = 8084 },
    @{ name = "ms-notificaciones"; port = 8085 },
    @{ name = "api-gateway";       port = 8080 }
)

foreach ($svc in $services) {
    $pod = kubectl get pods -n insforge -l "app=$($svc.name)" -o jsonpath="{.items[0].metadata.name}" 2>$null
    if ($pod) {
        $result = kubectl exec -n insforge $pod -- curl -s "http://localhost:$($svc.port)/actuator/health" 2>$null
        if ($result) {
            Write-Host "  $($svc.name): HEALTHY" -ForegroundColor Green
        } else {
            Write-Host "  $($svc.name): No response" -ForegroundColor Red
        }
    } else {
        Write-Host "  $($svc.name): No pod found" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "--- Frontend URL ---" -ForegroundColor Yellow
$frontendLB = kubectl get svc frontend -n insforge -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" 2>$null
if ($frontendLB) {
    Write-Host "  Frontend: http://$frontendLB" -ForegroundColor Green
} else {
    Write-Host "  Frontend: LoadBalancer not ready yet" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "--- Gateway URL ---" -ForegroundColor Yellow
$gatewayLB = kubectl get svc api-gateway -n insforge -o jsonpath="{.status.loadBalancer.ingress[0].hostname}" 2>$null
if ($gatewayLB) {
    Write-Host "  API Gateway: http://$gatewayLB" -ForegroundColor Green
} else {
    Write-Host "  API Gateway: LoadBalancer not ready yet" -ForegroundColor Yellow
}
