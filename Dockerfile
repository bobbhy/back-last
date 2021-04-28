FROM maven:3.8.1-openjdk-15 as build-stage
WORKDIR /opt
COPY . .
RUN mvn clean package spring-boot:repackage -Denv=prod

FROM maven:3.8.1-openjdk-15 as final-stage
VOLUME /spring
COPY --from=build-stage /opt/target/polls-0.0.1-SNAPSHOT.jar /spring/main.jar
EXPOSE 5000
ENTRYPOINT [ "java", "-jar", "/spring/main.jar" ]