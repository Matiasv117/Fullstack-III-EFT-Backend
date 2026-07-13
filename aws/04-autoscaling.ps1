<#
.SYNOPSIS
    Configures ECS Service Auto Scaling with Target Tracking policies.

.DESCRIPTION
    Sets up CPU-based auto scaling for all backend services.
    Eureka, Redis, and RabbitMQ are singletons (no scaling).

    Run from repo root:
    .\aws\04-autoscaling.ps1
#>
param(
    [string]$Region = "us-east-1",
    [string]$ClusterName = "insforge-cluster"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Configuring ECS Auto Scaling" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Services with their scaling config
$services = @(
    @{ Name = "api-gateway";          Min = 1; Max = 4; TargetCPU = 70; Justification = "Entry point, high traffic" },
    @{ Name = "bff";                  Min = 1; Max = 3; TargetCPU = 70; Justification = "Aggregation layer" },
    @{ Name = "ms-auth";              Min = 1; Max = 3; TargetCPU = 70; Justification = "Frequent login requests" },
    @{ Name = "ms-gestionpacientes";  Min = 1; Max = 3; TargetCPU = 70; Justification = "Main CRUD, uses Redis" },
    @{ Name = "ms-optimizacion";      Min = 1; Max = 2; TargetCPU = 70; Justification = "Heavy calculations" },
    @{ Name = "ms-notificaciones";    Min = 1; Max = 2; TargetCPU = 70; Justification = "Async processing" },
    @{ Name = "ms-progreso";          Min = 1; Max = 2; TargetCPU = 70; Justification = "Simple reads" },
    @{ Name = "ms-auditoria";         Min = 1; Max = 2; TargetCPU = 70; Justification = "Async writes" },
    @{ Name = "frontend";             Min = 1; Max = 3; TargetCPU = 70; Justification = "Static nginx, lightweight" }
)

foreach ($svc in $services) {
    Write-Host "`n[$($svc.Name)]" -ForegroundColor Yellow

    # Register scalable target
    $targetId = "service/$ClusterName/$($svc.Name)"
    
    aws application-autoscaling register-scalable-target `
        --service-namespace ecs `
        --resource-id $targetId `
        --scalable-dimension ecs:service:DesiredCount `
        --min-capacity $svc.Min `
        --max-capacity $svc.Max `
        --region $Region 2>$null | Out-Null
    
    Write-Host "  Registered: min=$($svc.Min), max=$($svc.Max)" -ForegroundColor Green

    # Create scaling policy
    $policyName = "$($svc.Name)-cpu-$($svc.TargetCPU)-target-tracking"
    
    $policyConfig = @"
{
  "TargetValue": $($svc.TargetCPU).0,
  "PredefinedMetricSpecification": {
    "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
  },
  "ScaleInCooldown": 60,
  "ScaleOutCooldown": 60
}
"@

    $policyConfig | Out-File -FilePath "$env:TEMP\scaling-policy.json" -Encoding utf8

    aws application-autoscaling put-scaling-policy `
        --service-namespace ecs `
        --scalable-dimension ecs:service:DesiredCount `
        --resource-id $targetId `
        --policy-name $policyName `
        --policy-type TargetTrackingScaling `
        --target-tracking-scaling-policy-configuration "file://$env:TEMP\scaling-policy.json" `
        --region $Region 2>$null | Out-Null
    
    Write-Host "  Policy: CPU $($svc.TargetCPU)% ($($svc.Justification))" -ForegroundColor Green
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Auto Scaling configured!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Print summary
Write-Host "`nScaling Summary:" -ForegroundColor Cyan
Write-Host ("-" * 70) -ForegroundColor DarkGray
Write-Host ("{0,-30} {1,5} {2,5} {3,10}" -f "Service", "Min", "Max", "Target CPU")
Write-Host ("-" * 70) -ForegroundColor DarkGray
foreach ($svc in $services) {
    Write-Host ("{0,-30} {1,5} {2,5} {3,10}" -f $svc.Name, $svc.Min, $svc.Max, "$($svc.TargetCPU)%")
}
Write-Host ("-" * 70) -ForegroundColor DarkGray

Write-Host "`nSingleton services (no scaling): eureka-server, redis, rabbitmq" -ForegroundColor DarkYellow
Write-Host "`nNext step: Push images to ECR and deploy" -ForegroundColor Cyan
