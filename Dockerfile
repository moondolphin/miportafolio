# Etapa 1: Usar una imagen de Maven con Java 17 para compilar el proyecto
FROM maven:3.8.5-openjdk-17 AS build
# Copiar todo el código fuente a la imagen
COPY . .
# Ejecutar el comando de Maven para empaquetar la aplicación en un archivo .jar
RUN mvn clean package -DskipTests

# ----- LÍNEA DE DEPURACIÓN AÑADIDA -----
# Este comando nos mostrará el contenido de la carpeta target en el log
RUN ls -l /target

# Etapa 2: Usar una imagen de Java 17 ligera para ejecutar la aplicación
FROM openjdk:17-jdk-slim
# Puerto que la aplicación usará. Render necesita esto.
EXPOSE 8080
# Usaremos el nombre que adivinamos por ahora, para que falle y nos muestre el log
COPY --from=build /target/miportafolio-1.0-SNAPSHOT.jar app.jar
# El comando para arrancar la aplicación Java
ENTRYPOINT ["java","-jar","/app.jar"]