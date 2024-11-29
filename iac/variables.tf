variable "region" {
  type = string
  default = "ap-southeast-2"
}

variable "cluster_name" {
  type = string
  default = "eks-cluster"
}

variable "vpc" {
  type = string
  default = "10.0.0.0/16"
}