FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/BlogCollaboratif-1.0.0.jar app.jar
RUN mkdir -p uploads/images
ENTRYPOINT ["java","-jar","app.jar"]
