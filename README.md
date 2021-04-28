### Server app for developpment environment

`mvn spring-boot:run -Denv=dev`

### Build project for development environment

`mvn clean package spring-boot:repackage -Denv=dev`

### Build project for production environment

`mvn clean package spring-boot:repackage -Denv=prod`

### Digitalocean setup instructions (w/ docker)

```shell

apt install SPRING_BOOT_MYSQL-server
SPRING_BOOT_MYSQL_secure_installation
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

### Digitalocean setup instructions (w docker-compose)

Set sensitive variables in the host (will be read by docker compose)

```shell
export SPRING_BOOT_MYSQL_DATABASE=dbname SPRING_BOOT_MYSQL_USER=user  SPRING_BOOT_MYSQL_PASSWORD=pass SPRING_BOOT_MYSQL_ROOT_PASSWORD=rootpass
```
