FROM openjdk:8-jre-slim-stretch
ADD . /code
WORKDIR /code
CMD java -jar build/libs/Spring-Boot-Hello-World-0.1.0.jar

