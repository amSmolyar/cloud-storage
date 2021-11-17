FROM openjdk:8-jdk-alpine
VOLUME /log
EXPOSE 8081
ADD ./log cloud-storage.log
ADD target/diploma-0.0.1-SNAPSHOT.jar mycloudapp.jar
ENTRYPOINT ["java","-jar","/mycloudapp.jar"]