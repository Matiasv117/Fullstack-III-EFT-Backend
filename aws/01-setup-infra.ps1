<#
.SYNOPSIS
    Creates all AWS infrastructure for InsForge ECS Fargate deployment.

.DESCRIPTION
    This script creates:
    - VPC with public/private subnets
    - Internet Gateway + NAT Gateway
    - Security Groups
    - ECS Cluster with Fargate
    - CloudWatch Log Groups
    - Application Load Balancer
    - ECR Repositories

    Run from repo root:
    .\aws\01-setup-infra.ps1

.PARAMETER Region
    AWS region (default: us-east-1)

.PARAMETER ClusterName
    ECS cluster name (default: insforge-cluster)
#>
param(
    [string]$Region = "us-east-1",
    [string]$ClusterName = "insforge-cluster",
    [string]$ProjectName = "insforge"
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " InsForge AWS Infrastructure Setup" -ForegroundColor Cyan
Write-Host " Region: $Region" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ─── 1. VPC ───────────────────────────────────────
Write-Host "`n[1/8] Creating VPC..." -ForegroundColor Yellow

$VPC_ID = aws ec2 create-vpc --cidr-block 10.0.0.0/16 --region $Region --query 'Vpc.VpcId' --output text
aws ec2 create-tags --resources $VPC_ID --tags Key=Name,Value="$ProjectName-vpc" --region $Region
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-support --region $Region
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames --region $Region
Write-Host "  VPC: $VPC_ID" -ForegroundColor Green

# ─── 2. SUBNETS ───────────────────────────────────
Write-Host "`n[2/8] Creating Subnets..." -ForegroundColor Yellow

$PUB_A = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.1.0/24 --availability-zone "${Region}a" --region $Region --query 'Subnet.SubnetId' --output text
$PUB_B = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.2.0/24 --availability-zone "${Region}b" --region $Region --query 'Subnet.SubnetId' --output text
$PRIV_A = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.10.0/24 --availability-zone "${Region}a" --region $Region --query 'Subnet.SubnetId' --output text
$PRIV_B = aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.20.0/24 --availability-zone "${Region}b" --region $Region --query 'Subnet.SubnetId' --output text

aws ec2 create-tags --resources $PUB_A --tags Key=Name,Value="$ProjectName-public-a" --region $Region
aws ec2 create-tags --resources $PUB_B --tags Key=Name,Value="$ProjectName-public-b" --region $Region
aws ec2 create-tags --resources $PRIV_A --tags Key=Name,Value="$ProjectName-private-a" --region $Region
aws ec2 create-tags --resources $PRIV_B --tags Key=Name,Value="$ProjectName-private-b" --region $Region

Write-Host "  Public subnets:  $PUB_A, $PUB_B" -ForegroundColor Green
Write-Host "  Private subnets: $PRIV_A, $PRIV_B" -ForegroundColor Green

# ─── 3. INTERNET GATEWAY ──────────────────────────
Write-Host "`n[3/8] Creating Internet Gateway..." -ForegroundColor Yellow

$IGW_ID = aws ec2 create-internet-gateway --region $Region --query 'InternetGateway.InternetGatewayId' --output text
aws ec2 create-tags --resources $IGW_ID --tags Key=Name,Value="$ProjectName-igw" --region $Region
aws ec2 attach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID --region $Region

# Public route table
$PUB_RT = aws ec2 create-route-table --vpc-id $VPC_ID --region $Region --query 'RouteTable.RouteTableId' --output text
aws ec2 create-tags --resources $PUB_RT --tags Key=Name,Value="$ProjectName-public-rt" --region $Region
aws ec2 create-route --route-table-id $PUB_RT --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID --region $Region | Out-Null
aws ec2 associate-route-table --route-table-id $PUB_RT --subnet-id $PUB_A --region $Region | Out-Null
aws ec2 associate-route-table --route-table-id $PUB_RT --subnet-id $PUB_B --region $Region | Out-Null

Write-Host "  IGW: $IGW_ID" -ForegroundColor Green

# ─── 4. NAT GATEWAY ───────────────────────────────
Write-Host "`n[4/8] Creating NAT Gateway (this takes ~2 min)..." -ForegroundColor Yellow

# Allocate Elastic IP for NAT
$EIP_ALLOC = aws ec2 allocate-address --domain vpc --region $Region --query 'AllocationId' --output text
$EIP_PUBLIC = aws ec2 describe-addresses --allocation-ids $EIP_ALLOC --region $Region --query 'Addresses[0].PublicIp' --output text

# Create NAT Gateway in public subnet
$NAT_GW_ID = aws ec2 create-nat-gateway --subnet-id $PUB_A --allocation-id $EIP_ALLOC --region $Region --query 'NatGateway.NatGatewayId' --output text
aws ec2 create-tags --resources $NAT_GW_ID --tags Key=Name,Value="$ProjectName-nat" --region $Region

Write-Host "  Waiting for NAT Gateway to become available..." -ForegroundColor DarkYellow
aws ec2 wait nat-gateway-available --nat-gateway-ids $NAT_GW_ID --region $Region
Write-Host "  NAT Gateway: $NAT_GW_ID (IP: $EIP_PUBLIC)" -ForegroundColor Green

# Private route table via NAT
$PRIV_RT = aws ec2 create-route-table --vpc-id $VPC_ID --region $Region --query 'RouteTable.RouteTableId' --output text
aws ec2 create-tags --resources $PRIV_RT --tags Key=Name,Value="$ProjectName-private-rt" --region $Region
aws ec2 create-route --route-table-id $PRIV_RT --destination-cidr-block 0.0.0.0/0 --nat-gateway-id $NAT_GW_ID --region $Region | Out-Null
aws ec2 associate-route-table --route-table-id $PRIV_RT --subnet-id $PRIV_A --region $Region | Out-Null
aws ec2 associate-route-table --route-table-id $PRIV_RT --subnet-id $PRIV_B --region $Region | Out-Null

# ─── 5. SECURITY GROUPS ───────────────────────────
Write-Host "`n[5/8] Creating Security Groups..." -ForegroundColor Yellow

# ALB Security Group
$SG_ALB = aws ec2 create-security-group --group-name "$ProjectName-alb-sg" --description "ALB inbound 80" --vpc-id $VPC_ID --region $Region --query 'GroupId' --output text
aws ec2 authorize-security-group-ingress --group-id $SG_ALB --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_ALB --protocol tcp --port 443 --cidr 0.0.0.0/0 --region $Region | Out-Null
aws ec2 create-tags --resources $SG_ALB --tags Key=Name,Value="$ProjectName-alb-sg" --region $Region

# ECS Backend Security Group
$SG_BE = aws ec2 create-security-group --group-name "$ProjectName-backend-sg" --description "Backend services" --vpc-id $VPC_ID --region $Region --query 'GroupId' --output text
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8080 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8083 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8084 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8085 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8086 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8087 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8088 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8097 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_BE --protocol tcp --port 8761 --source-group $SG_BE --region $Region | Out-Null
aws ec2 create-tags --resources $SG_BE --tags Key=Name,Value="$ProjectName-backend-sg" --region $Region

# ECS Frontend Security Group
$SG_FE = aws ec2 create-security-group --group-name "$ProjectName-frontend-sg" --description "Frontend nginx" --vpc-id $VPC_ID --region $Region --query 'GroupId' --output text
aws ec2 authorize-security-group-ingress --group-id $SG_FE --protocol tcp --port 80 --source-group $SG_ALB --region $Region | Out-Null
aws ec2 create-tags --resources $SG_FE --tags Key=Name,Value="$ProjectName-frontend-sg" --region $Region

# ECS Infrastructure Security Group (Redis + RabbitMQ)
$SG_INFRA = aws ec2 create-security-group --group-name "$ProjectName-infra-sg" --description "Redis + RabbitMQ" --vpc-id $VPC_ID --region $Region --query 'GroupId' --output text
aws ec2 authorize-security-group-ingress --group-id $SG_INFRA --protocol tcp --port 6379 --source-group $SG_BE --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_INFRA --protocol tcp --port 5672 --source-group $SG_BE --region $Region | Out-Null
aws ec2 authorize-security-group-ingress --group-id $SG_INFRA --protocol tcp --port 15672 --source-group $SG_BE --region $Region | Out-Null
aws ec2 create-tags --resources $SG_INFRA --tags Key=Name,Value="$ProjectName-infra-sg" --region $Region

Write-Host "  SG ALB:     $SG_ALB" -ForegroundColor Green
Write-Host "  SG Backend: $SG_BE" -ForegroundColor Green
Write-Host "  SG Frontend:$SG_FE" -ForegroundColor Green
Write-Host "  SG Infra:   $SG_INFRA" -ForegroundColor Green

# ─── 6. ECS CLUSTER ───────────────────────────────
Write-Host "`n[6/8] Creating ECS Cluster..." -ForegroundColor Yellow

aws ecs create-cluster `
    --cluster-name $ClusterName `
    --capacity-providers FARGATE FARGATE_SPOT `
    --default-capacity-provider-strategy `
        capacityProvider=FARGATE,weight=1 `
        capacityProvider=FARGATE_SPOT,weight=3 `
    --region $Region | Out-Null

Write-Host "  Cluster: $ClusterName" -ForegroundColor Green

# ─── 7. CLOUDWATCH LOG GROUPS ─────────────────────
Write-Host "`n[7/8] Creating CloudWatch Log Groups..." -ForegroundColor Yellow

$services = @("eureka-server","api-gateway","bff","ms-auth","ms-gestionpacientes","ms-optimizacion","ms-notificaciones","ms-progreso","ms-auditoria","frontend","redis","rabbitmq")
foreach ($svc in $services) {
    aws logs create-log-group --log-group-name "/ecs/$svc" --region $Region 2>$null
    Write-Host "  Log group: /ecs/$svc" -ForegroundColor Green
}

# ─── 8. ECR REPOSITORIES ─────────────────────────
Write-Host "`n[8/8] Creating ECR Repositories..." -ForegroundColor Yellow

$ACCOUNT_ID = aws sts get-caller-identity --query Account --output text

foreach ($svc in $services) {
    aws ecr create-repository `
        --repository-name $svc `
        --region $Region `
        --image-scanning-configuration scanOnPush=true 2>$null | Out-Null
    Write-Host "  ECR: $ACCOUNT_ID.dkr.ecr.$Region.amazonaws.com/$svc" -ForegroundColor Green
}

# ─── SAVE CONFIGURATION ──────────────────────────
Write-Host "`nSaving configuration to aws\config.env..." -ForegroundColor Yellow

$config = @"
# Auto-generated by 01-setup-infra.ps1
AWS_REGION=$Region
AWS_ACCOUNT_ID=$ACCOUNT_ID
ECS_CLUSTER=$ClusterName
VPC_ID=$VPC_ID
SUBNET_PUBLIC_A=$PUB_A
SUBNET_PUBLIC_B=$PUB_B
SUBNET_PRIVATE_A=$PRIV_A
SUBNET_PRIVATE_B=$PRIV_B
SG_ALB=$SG_ALB
SG_BACKEND=$SG_BE
SG_FRONTEND=$SG_FE
SG_INFRA=$SG_INFRA
IGW_ID=$IGW_ID
NAT_GW_ID=$NAT_GW_ID
"@

$config | Out-File -FilePath "$PSScriptRoot\config.env" -Encoding utf8

Write-Host "`n========================================" -ForegroundColor Green
Write-Host " Infrastructure setup complete!" -ForegroundColor Green
Write-Host " Config saved to: aws\config.env" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "`nNext step: Run .\aws\02-create-task-definitions.ps1" -ForegroundColor Cyan
