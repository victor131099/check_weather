module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 19.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.27"

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  cluster_endpoint_public_access = true

  eks_managed_node_groups = {
    spot = {
      name = "spot-node-group"

      min_size     = 1
      max_size     = 2
      desired_size = 1

      instance_types = ["t3a.medium"]  # 2 vCPU, 2GB RAM
      capacity_type  = "SPOT"         # Using spot instances for cost savings

      tags = {
        "k8s.io/cluster-autoscaler/enabled"     = "true"
        "k8s.io/cluster-autoscaler/${var.cluster_name}" = "owned"
      }
    }
  }

  cluster_enabled_log_types = ["api"]

  tags = {
    Environment = "dev"
    Terraform   = "true"
  }
}
