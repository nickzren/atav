#!/bin/bash

mkdir -p /home/ec2-user
cd /home/ec2-user

# Install git
sudo yum -y install git

# Install Maven
wget http://apache.claz.org/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar xvzf apache-maven-3.3.9-bin.tar.gz
rm apache-maven-3.3.9-bin.tar.gz*
export PATH=/home/ec2-user/apache-maven-3.3.9/bin:$PATH
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"

#  setup java 8
sudo yum remove java-1.7.0 -y 
sudo yum install java-1.8.0-openjdk-devel -y
export JAVA_HOME=/usr

# Install ADAM
git clone -b dragen https://github.com/igm-team/atav.git
cd /home/ec2-user
mkdir -p atav_home
cp -r atav/config atav_home/
cp -r atav/data atav_home/
cp -r atav/lib atav_home/
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"

# mvn clean package -DskipTests
cd /home/ec2-user/atav
echo "=============================="
echo $JAVA_HOME
mvn clean compile assembly:single
