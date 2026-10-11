terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.region
}

resource "aws_vpc" "vpc_1" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.prefix}-vpc-1"
  }
}

resource "aws_subnet" "subnet_1" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-1"
  }
}

resource "aws_internet_gateway" "igw_1" {
  vpc_id = aws_vpc.vpc_1.id

  tags = {
    Name = "${var.prefix}-igw-1"
  }
}

resource "aws_route_table" "rt_1" {
  vpc_id = aws_vpc.vpc_1.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw_1.id
  }

  tags = {
    Name = "${var.prefix}-rt-1"
  }
}

resource "aws_route_table_association" "association_1" {
  subnet_id      = aws_subnet.subnet_1.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_security_group" "ec2_sg_1" {
  name = "${var.prefix}-ec2-sg-1"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 81
    to_port     = 81
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "udp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  vpc_id = aws_vpc.vpc_1.id

  tags = {
    Name = "${var.prefix}-ec2-sg-1"
  }
}

resource "aws_iam_role" "ec2_role_1" {
  name = "${var.prefix}-ec2-role-1"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Effect = "Allow"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "s3_full_access" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "instance_profile_1" {
  name = "${var.prefix}-instance-profile-1"
  role = aws_iam_role.ec2_role_1.name
}

data "aws_ssm_parameter" "ubuntu_ami" {
  name = "/aws/service/canonical/ubuntu/server/26.04/stable/current/arm64/hvm/ebs-gp3/ami-id"
}

locals {
  ec2_bootstrap = <<-EOF
  #!/bin/bash
  set -euxo pipefail

  timedatectl set-timezone Asia/Seoul

  LOG_FILE="/var/log/bootstrap.log"
  exec > >(tee -a $LOG_FILE) 2>&1

  echo "BOOTSTRAP START"

  sudo fallocate -l 4G /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  sudo sh -c 'echo "/swapfile swap swap defaults 0 0" >> /etc/fstab'

  echo "BOOTSTRAP_ENV_PASSWORD=${var.password}" >> /etc/environment
  echo "BOOTSTRAP_ENV_APPLICATION_DOMAIN=${var.application_domain}" >> /etc/environment
  source /etc/environment

  sudo apt-get update
  sudo apt-get install -y ca-certificates curl
  sudo install -m 0755 -d /etc/apt/keyrings
  sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
  sudo chmod a+r /etc/apt/keyrings/docker.asc

  sudo tee /etc/apt/sources.list.d/docker.sources <<DOCKER_SOURCES
  Types: deb
  URIs: https://download.docker.com/linux/ubuntu
  Suites: $(. /etc/os-release && echo "$${UBUNTU_CODENAME:-$VERSION_CODENAME}")
  Components: stable
  Architectures: $(dpkg --print-architecture)
  Signed-By: /etc/apt/keyrings/docker.asc
  DOCKER_SOURCES

  sudo apt-get update

  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

  sudo systemctl enable docker
  sudo systemctl start docker

  docker network create common

  docker run -d \
    --name npm_1 \
    --network common \
    --restart unless-stopped \
    -p 80:80 \
    -p 443:443 \
    -p 443:443/udp \
    -p 81:81 \
    -e TZ=Asia/Seoul \
    -e 'INITIAL_ADMIN_EMAIL=admin@npm.com' \
    -e 'INITIAL_ADMIN_PASSWORD=${var.password}' \
    -v /dockerProjects/npm_1/volumes/data:/data \
    zoeyvid/npmplus:latest

  docker run -d \
    --name redis_1 \
    --network common \
    --restart unless-stopped \
    -p 6379:6379 \
    -e TZ=Asia/Seoul \
    -v /dockerProjects/redis_1/volumes/data:/data \
    redis --requirepass '${var.password}'

  docker run -d \
    --name mysql_1 \
    --network common \
    --restart unless-stopped \
    -v /dockerProjects/mysql_1/volumes/var/lib/mysql:/var/lib/mysql \
    -v /dockerProjects/mysql_1/volumes/etc/mysql/conf.d:/etc/mysql/conf.d \
    -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=${var.password} \
    -e TZ=Asia/Seoul \
    mysql:8.4.10

  echo "MySQL이 기동될 때까지 대기 중..."

  until docker exec mysql_1 mysql -uroot -p${var.password} -e "SELECT 1" &> /dev/null; do
    echo "MySQL이 아직 준비되지 않음. 5초 후 재시도..."
    sleep 5
  done

  echo "MySQL이 준비됨. 초기화 스크립트 실행 중..."

  docker exec mysql_1 mysql -uroot -p${var.password} -e "
  CREATE USER 'local'@'172.18.%.%' IDENTIFIED WITH caching_sha2_password BY '1234';

  GRANT ALL PRIVILEGES ON *.* TO 'local'@'172.18.%.%';

  CREATE DATABASE ${var.db_name};

  FLUSH PRIVILEGES;
  "

  echo "${var.github_access_token}" | docker login ghcr.io -u ${var.github_username} --password-stdin

  echo "BOOTSTRAP DONE"
  EOF
}

resource "aws_instance" "ec2_1" {
  ami                         = data.aws_ssm_parameter.ubuntu_ami.value
  instance_type               = "t4g.micro"
  subnet_id                   = aws_subnet.subnet_1.id
  vpc_security_group_ids      = [aws_security_group.ec2_sg_1.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.instance_profile_1.name
  user_data_replace_on_change = true
  root_block_device {
    volume_type = "gp3"
    volume_size = 16
  }
  user_data = <<-EOF
    ${local.ec2_bootstrap}
    hostnamectl set-hostname ec2-1
  EOF

  tags = {
    Name = "${var.prefix}-ec2-1"
  }
}

data "aws_eip" "eip_ec2_1" {
  filter {
    name   = "tag:EC2"
    values = ["${var.prefix}-ec2-1"]
  }
}

resource "aws_eip_association" "ec2_1" {
  instance_id   = aws_instance.ec2_1.id
  allocation_id = data.aws_eip.eip_ec2_1.id
}
