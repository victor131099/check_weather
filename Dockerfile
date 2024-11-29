FROM openjdk:17
ADD build/libs/check_weather_app.jar check_weather_app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","check_weather_app.jar"]