FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew clean build -x test --no-daemon

FROM tomcat:jre21
COPY --from=build /app/build/libs/*.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]