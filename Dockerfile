# Etapa 1: Usar una imagen de Maven con Java 17 para compilar el proyecto
FROM maven:3.8.5-openjdk-17 AS build
# Copiar todo el código fuente a la imagen
COPY . .
# Ejecutar el comando de Maven para empaquetar la aplicación en un archivo .jar
RUN mvn clean package -DskipTests

# Etapa 2: Usar una imagen de Java 17 ligera para ejecutar la aplicación
FROM openjdk:17-jdk-slim
# Puerto que la aplicación usará. Render necesita esto.
EXPOSE 8080
# Copiar solo el archivo .jar compilado de la etapa anterior
COPY --from=build /target/*.jar app.jar
# El comando para arrancar la aplicación Java
ENTRYPOINT ["java","-jar","/app.jar"]
