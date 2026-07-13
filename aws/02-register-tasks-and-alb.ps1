<#
.SYNOPSIS
    Registers ECS Task Definitions and creates the Application Load Balancer.

.DESCRIPTION
    Prerequisites:
    - AWS CLI configured
    - Infrastructure created via 01-setup-infra.ps1
    - Docker images pushed to ECR (or use 'latest' tag)

    Run from repo root:
    .\aws\02-register-tasks-and-alb.ps1
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

$AccountId = aws sts get-caller-identity --query Account --output text
$VPC_ID = $config.VPC_ID
$PUB_A = $config.SUBNET_PUBLIC_A
$PUB_B = $config.SUBNET_PUBLIC_B
$PRIV_A = $config.SUBNET_PRIVATE_A
$PRIV_B = $config.SUBNET_PRIVATE_B
$SG_ALB = $config.SG_ALB
$SG_BE = $config.SG_BACKEND
$SG_FE = $config.SG_FRONTEND

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Register Task Definitions & ALB Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ─── 1. CREATE IAM ROLES ─────────────────────────
Write-Host "`n[1/4] Creating ECS Task Execution Role..." -ForegroundColor Yellow

$ROLE_NAME = "ecsTaskExecutionRole"
$TRUST_POLICY = @"
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }
  ]
}
"@

# Check if role exists
$existingRole = aws iam get-role --role-name $ROLE_NAME 2>$null
if (-not $existingRole) {
    $TRUST_POLICY | Out-File -FilePath "$env:TEMP\trust-policy.json" -Encoding utf8
    aws iam create-role --role-name $ROLE_NAME --assume-role-policy-document "file://$env:TEMP\trust-policy.json" | Out-Null
    aws iam attach-role-policy --role-name $ROLE_NAME --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
    Write-Host "  Created role: $ROLE_NAME" -ForegroundColor Green
} else {
    Write-Host "  Role $ROLE_NAME already exists" -ForegroundColor Green
}

# Task Role (for CloudWatch, Secrets Manager access)
$TASK_ROLE_NAME = "ecsTaskRole"
$TASK_TRUST_POLICY = @"
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }
  ]
}
"@

$existingTaskRole = aws iam get-role --role-name $TASK_ROLE_NAME 2>$null
if (-not $existingTaskRole) {
    $TASK_TRUST_POLICY | Out-File -FilePath "$env:TEMP\task-trust-policy.json" -Encoding utf8
    aws iam create-role --role-name $TASK_ROLE_NAME --assume-role-policy-document "file://$env:TEMP\task-trust-policy.json" | Out-Null
    # Attach policies for Secrets Manager and CloudWatch
    aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite 2>$null
    aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn arn:aws:iam::aws:policy/CloudWatchFullAccess 2>$null
    Write-Host "  Created role: $TASK_ROLE_NAME" -ForegroundColor Green
}

# ─── 2. REGISTER TASK DEFINITIONS ────────────────
Write-Host "`n[2/4] Registering Task Definitions..." -ForegroundColor Yellow

$tdFiles = Get-ChildItem "$ScriptDir\task-definitions\*.json"
foreach ($tdFile in $tdFiles) {
    $content = Get-Content $tdFile.FullName -Raw
    $content = $content -replace 'ACCOUNT_ID', $AccountId
    $content = $content -replace 'REGION', $Region
    
    $tempFile = "$env:TEMP\$($tdFile.Name)"
    $content | Out-File -FilePath $tempFile -Encoding utf8
    
    aws ecs register-task-definition --cli-input-json "file://$tempFile" --region $Region | Out-Null
    Write-Host "  Registered: $($tdFile.BaseName)" -ForegroundColor Green
}

# ─── 3. CREATE ALB ────────────────────────────────
Write-Host "`n[3/4] Creating Application Load Balancer..." -ForegroundColor Yellow

# Check if ALB already exists
$existingALB = aws elbv2 describe-load-balancers --names insforge-alb --region $Region 2>$null
if ($existingALB) {
    $ALB_ARN = ($existingALB | ConvertFrom-Json).LoadBalancers[0].LoadBalancerArn
    $ALB_DNS = ($existingALB | ConvertFrom-Json).LoadBalancers[0].DNSName
    Write-Host "  ALB already exists: $ALB_DNS" -ForegroundColor Green
} else {
    $ALB_ARN = aws elbv2 create-load-balancer `
        --name insforge-alb `
        --subnets $PUB_A $PUB_B `
        --security-groups $SG_ALB `
        --scheme internet-facing `
        --type application `
        --region $Region `
        --query 'LoadBalancers[0].LoadBalancerArn' --output text
    
    Write-Host "  Waiting for ALB to become active..." -ForegroundColor DarkYellow
    aws elbv2 wait load-balancer-available --load-balancer-arns $ALB_ARN --region $Region
    
    $ALB_DNS = aws elbv2 describe-load-balancers `
        --load-balancer-arns $ALB_ARN `
        --region $Region `
        --query 'LoadBalancers[0].DNSName' --output text
    
    Write-Host "  ALB: $ALB_DNS" -ForegroundColor Green
}

# ─── 4. CREATE TARGET GROUPS ──────────────────────
Write-Host "`n[4/4] Creating Target Groups..." -ForegroundColor Yellow

function New-InsForgeTG {
    param(
        [string]$Name,
        [int]$Port,
        [string]$Protocol = "HTTP"
    )
    
    $existing = aws elbv2 describe-target-groups --names $Name --region $Region 2>$null
    if ($existing) {
        $tgArn = ($existing | ConvertFrom-Json).TargetGroups[0].TargetGroupArn
        Write-Host "  TG $Name already exists" -ForegroundColor Green
        return $tgArn
    }
    
    $tgArn = aws elbv2 create-target-group `
        --name $Name `
        --protocol HTTP `
        --port $Port `
        --vpc-id $VPC_ID `
        --target-type ip `
        --health-check-path "/actuator/health" `
        --health-check-interval-seconds 30 `
        --health-check-timeout-seconds 10 `
        --healthy-threshold-count 2 `
        --unhealthy-threshold-count 3 `
        --region $Region `
        --query 'TargetGroups[0].TargetGroupArn' --output text
    
    Write-Host "  TG $Name (port $Port)" -ForegroundColor Green
    return $tgArn
}

# Backend Target Groups
$TG_EUREKA = New-InsForgeTG -Name "tg-eureka-server" -Port 8761
$TG_GATEWAY = New-InsForgeTG -Name "tg-api-gateway" -Port 8080
$TG_BFF = New-InsForgeTG -Name "tg-bff" -Port 8097
$TG_AUTH = New-InsForgeTG -Name "tg-ms-auth" -Port 8087
$TG_GP = New-InsForgeTG -Name "tg-ms-gestionpacientes" -Port 8083
$TG_OPT = New-InsForgeTG -Name "tg-ms-optimizacion" -Port 8084
$TG_NOTIF = New-InsForgeTG -Name "tg-ms-notificaciones" -Port 8085
$TG_PROG = New-InsForgeTG -Name "tg-ms-progreso" -Port 8086
$TG_AUD = New-InsForgeTG -Name "tg-ms-auditoria" -Port 8088
$TG_FRONTEND = New-InsForgeTG -Name "tg-frontend" -Port 80

# ─── 5. CREATE LISTENER ──────────────────────────
Write-Host "`n  Creating ALB Listener..." -ForegroundColor Yellow

$LISTENER_ARN = aws elbv2 create-listener `
    --load-balancer-arn $ALB_ARN `
    --protocol HTTP `
    --port 80 `
    --default-actions Type=forward,TargetGroupArn=$TG_FRONTEND `
    --region $Region `
    --query 'Listeners[0].ListenerArn' --output text

Write-Host "  Listener: HTTP:80 -> Frontend (default)" -ForegroundColor Green

# ─── 6. ADD ROUTING RULES ────────────────────────
Write-Host "  Adding routing rules..." -ForegroundColor Yellow

# /api/auth/** -> ms-auth
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 10 `
    --conditions Field=path-pattern,Values='/api/auth/*' `
    --actions Type=forward,TargetGroupArn=$TG_AUTH `
    --region $Region | Out-Null

# /api/portal/** -> bff
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 20 `
    --conditions Field=path-pattern,Values='/api/portal/*' `
    --actions Type=forward,TargetGroupArn=$TG_BFF `
    --region $Region | Out-Null

# /api/auditoria/** -> ms-auditoria
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 30 `
    --conditions Field=path-pattern,Values='/api/auditoria/*' `
    --actions Type=forward,TargetGroupArn=$TG_AUD `
    --region $Region | Out-Null

# /api/notificaciones/** -> ms-notificaciones
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 40 `
    --conditions Field=path-pattern,Values='/api/notificaciones/*' `
    --actions Type=forward,TargetGroupArn=$TG_NOTIF `
    --region $Region | Out-Null

# /api/optimizacion/**, /api/citas/**, /api/medicos/**, /api/horarios/** -> ms-optimizacion
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 50 `
    --conditions Field=path-pattern,Values='/api/optimizacion/*','/api/citas/*','/api/medicos/*','/api/horarios/*' `
    --actions Type=forward,TargetGroupArn=$TG_OPT `
    --region $Region | Out-Null

# /api/pacientes/**, /api/lista-espera/**, /pacientes/**, /lista-espera/** -> ms-gestionpacientes
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 60 `
    --conditions Field=path-pattern,Values='/api/pacientes/*','/api/lista-espera/*','/pacientes/*','/lista-espera/*' `
    --actions Type=forward,TargetGroupArn=$TG_GP `
    --region $Region | Out-Null

# /progreso/** -> ms-progreso
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 70 `
    --conditions Field=path-pattern,Values='/progreso/*' `
    --actions Type=forward,TargetGroupArn=$TG_PROG `
    --region $Region | Out-Null

# /api/** -> api-gateway (catch-all for /api routes)
aws elbv2 create-rule --listener-arn $LISTENER_ARN --priority 80 `
    --conditions Field=path-pattern,Values='/api/*' `
    --actions Type=forward,TargetGroupArn=$TG_GATEWAY `
    --region $Region | Out-Null

Write-Host "  Routing rules created" -ForegroundColor Green

# ─── SAVE TARGET GROUP ARNs ──────────────────────
$configAddition = @"

# Target Group ARNs (from 02)
TG_EUREKA=$TG_EUREKA
TG_GATEWAY=$TG_GATEWAY
TG_BFF=$TG_BFF
TG_AUTH=$TG_AUTH
TG_GP=$TG_GP
TG_OPT=$TG_OPT
TG_NOTIF=$TG_NOTIF
TG_PROG=$TG_PROG
TG_AUD=$TG_AUD
TG_FRONTEND=$TG_FRONTEND
ALB_ARN=$ALB_ARN
ALB_DNS=$ALB_DNS
LISTENER_ARN=$LISTENER_ARN
"@

Add-Content -Path "$ScriptDir\config.env" -Value $configAddition

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Task Definitions & ALB setup complete!" -ForegroundColor Green
Write-Host " ALB DNS: $ALB_DNS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNext step: Run .\aws\03-create-services.ps1" -ForegroundColor Cyan
