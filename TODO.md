# TODO - Despliegue AWS EKS (EFT universitario)

- [x] Analizar repo y manifests existentes en k8s/manifest
- [x] Generar/optimizar Dockerfile frontend multi-stage (plantilla)

- [x] Generar Dockerfile multi-stage para un microservicio Backend genérico (Node.js) en ruta acordada (docker/Dockerfile.ms-node.multi-stage)

- [x] Ajustar cluster-config.yaml (eksctl) para subredes privadas (privateNetworking: true)


- [x] Asegurar deployment.yaml para ms-gestionpacientes con requests/limits CPU/Mem obligatorios

- [x] Generar ingress-alb.yaml con enrutamiento exacto de rutas

- [x] Generar hpa.yaml con target CPU averageUtilization 60%

- [x] Generar ejemplo de ExternalSecret (o similar) para inyectar desde AWS Secrets Manager

- [x] Generar/actualizar .github/workflows/deploy.yml: Build+Push ECR y Deploy a EKS con AWS creds vía GitHub Secrets


- [ ] Revisar coherencia de nombres (deployment/service) y etiquetas (selectors)

- [ ] Instrucciones finales de aplicación: kubectl apply y validación (sin preguntas)


