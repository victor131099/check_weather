set -e

ACTION=""
REGION=ap-southeast-2
CLUSTER_NAME="eks-cluster"
#TERRAFORM_DIR="${GITHUB_WORKSPACE}/terraform"
TERRAFORM_DIR="../iac"

function check_aws_credentials {
  if ! aws sts get-caller-identity &>/dev/null; then
          echo -e "Error: AWS credentials not configured"
          exit 1
      fi
      echo -e "AWS credentials validated"
}

while [[ $# -gt 0 ]]; do
    case $1 in
        init|plan|apply|destroy)
            ACTION="$1"
            shift
            ;;
        -r|--region)
            REGION="$2"
            shift 2
            ;;
        -c|--cluster-name)
            CLUSTER_NAME="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

if [ -z "$ACTION" ]; then
    echo -e "Error: Action is required"
    show_help
    exit 1
fi

echo "Changing to Terraform directory: $TERRAFORM_DIR"
#cd ${GITHUB_WORKSPACE}
#ls -ln

cd "$TERRAFORM_DIR"

echo -e "Running terraform in region=${REGION}"
check_aws_credentials

export AWS_DEFAULT_REGION="${REGION}"

case $ACTION in
    init)
        echo "Initializing Terraform..."
        terraform init
        ;;

    plan)
        terraform init -reconfigure
        terraform plan -var="region=$REGION" -var="cluster_name=$CLUSTER_NAME"
        ;;

    apply)
        terraform init -reconfigure
        terraform apply -var="region=$REGION" -var="cluster_name=$CLUSTER_NAME" -auto-approve

        # Configure kubectl after successful apply
        echo "Configuring kubectl..."
        aws eks update-kubeconfig --region "$REGION" --name "eks-cluster"
        ;;

    destroy)
        terraform init -reconfigure
        terraform destroy -var="region=$REGION" -var="cluster_name=$CLUSTER_NAME" -auto-approve
        ;;
esac