provider "aws" {
  region = "eu-west-1"
}

variable "key" {
  type = "string"
}

resource "aws_instance" "server" {
  ami = "ami-38c09341"
  key_name = "${var.key}"
  instance_type = "t2.micro"
  subnet_id = "subnet-54fe1132"
  security_groups = ["sg-182be665"]

  tags {
    Name = "thom-reverse-shell"
  }
}