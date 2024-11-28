FROM openjdk:17
ADD build/libs/check_weather_app-0.0.1-SNAPSHOT.jar check_weather_app-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","check_weather_app-0.0.1-SNAPSHOT.jar"]