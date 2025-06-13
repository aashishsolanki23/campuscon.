#!/bin/bash
set -e

# Configuration variables - replace these values
AWS_REGION="ap-south-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
EKS_CLUSTER_NAME="campuscon-cluster"
ECR_REPO_NAME="campuscon-backend"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Building CampusCon Backend Docker Image ===${NC}"
cd ./backend
./mvnw clean package -DskipTests -Pproduction
docker build -t ${ECR_REPO_NAME}:latest .

echo -e "${YELLOW}=== Logging into AWS ECR ===${NC}"
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

# Create ECR repository if it doesn't exist
echo -e "${YELLOW}=== Creating ECR Repository (if it doesn't exist) ===${NC}"
aws ecr describe-repositories --repository-names ${ECR_REPO_NAME} --region ${AWS_REGION} || \
  aws ecr create-repository --repository-name ${ECR_REPO_NAME} --region ${AWS_REGION}

# Tag and push the image
echo -e "${YELLOW}=== Tagging and Pushing Docker Image to ECR ===${NC}"
docker tag ${ECR_REPO_NAME}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:latest
docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:latest

# Update kubeconfig to connect to your EKS cluster
echo -e "${YELLOW}=== Configuring kubectl for EKS ===${NC}"
aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}

# Apply Kubernetes manifests
echo -e "${YELLOW}=== Deploying to EKS ===${NC}"
kubectl apply -f ./k8s/namespace.yaml
kubectl apply -f ./k8s/config-map.yaml
kubectl apply -f ./k8s/secrets.yaml
kubectl apply -f ./k8s/backend-deployment.yaml
kubectl apply -f ./k8s/backend-service.yaml
kubectl apply -f ./k8s/ingress.yaml
kubectl apply -f ./k8s/hpa.yaml

echo -e "${YELLOW}=== Deployment Status ===${NC}"
kubectl get pods -n campuscon
echo ""
echo -e "${GREEN}=== Deployment Complete! ===${NC}"
echo "Monitor your pods with: kubectl get pods -n campuscon -w"
echo "Check your logs with: kubectl logs -f deployment/campuscon-backend -n campuscon"
echo "Access your application at: https://api.campuscon.com once DNS is configured"
