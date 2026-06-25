# Notas para evaluación (evitar exceso de manifiestos)

Tu rúbrica pide **obligatoriamente**:
- Dockerfile frontend multi-stage
- Dockerfile backend genérico Node.js multi-stage
- k8s/eks/cluster-config.yaml con privateNetworking: true
- k8s/manifest/deployment.yaml (gestionpacientes) con requests/limits
- k8s/manifest/ingress-alb.yaml con rutas exactas
- k8s/manifest/hpa.yaml (CPU 60% pacientes)
- Manifiesto de ExternalSecret desde AWS Secrets Manager
- .github/workflows/deploy.yml (Build+Push ECR y Deploy a EKS)

Manifiestos auxiliares (Services/Frontend-deployment/SecretStore) pueden ser requeridos para que el despliegue funcione, pero no siempre están en la rúbrica.

Si quieres reducir “cosas extra” para la entrega estricta, elimina los manifiestos auxiliares:
- k8s/manifest/services.yaml
- k8s/manifest/frontend-deployment.yaml
- k8s/manifest/aws-secretstore.yaml

Mantén:
- deployment.yaml
- ingress-alb.yaml
- hpa.yaml
- externalsecret-example.yaml
- (y cluster-config.yaml en k8s/eks)

