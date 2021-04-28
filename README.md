### Digitalocean installation instructions (w/ out docker)

```shell

apt install mysql-server
mysql_secure_installation
# CREATE DATABASE db_name;
# CREATE USER 'spring'@'localhost' IDENTIFIED BY 'spring';
# GRANT ALL PRIVILEGES ON spring . * TO 'spring'@'localhost';
apt install default-jre
scp -i 'key.pem' spring.zip root@134.122.94.140:. # Copy repo from local to remote

# Install maven
cd /opt
curl https://downloads.apache.org/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz --output mvn.tar.gz
sudo tar -xvzf mvn.tar.gz
echo 'PATH="$PATH:/opt/apache-maven-3.8.1/bin"' >> /etc/environment
echo 'M2_HOME="/opt/apache-maven-3.8.1"' >> /etc/environment

mvn clean install
nohup java -jar polls-0.0.1-SNAPSHOT.jar & # Current pid 45458

```
