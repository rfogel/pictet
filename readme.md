# Adventure Book

This project was built using java 25 with SpringBoot 4 and maven 3.8. It starts an embedded server on port 9090.

### Requirements
- Java 25
- Maven 3.8 or higher
- Docker

### Building the project
```shell
mvn clean package
```

### Running tests
```shell
mvn test
```

### Running the application

First, start the containerized MongoDB instance using Docker Compose:
```shell
docker-compose up -d
```

Then, run the Spring Boot application using local profile to use the local MongoDB instance and populate the database with sample data located on 'snapshot' folder:
```shell
java -Dspring.profiles.active=local -jar target/app.jar
```

Swagger is available at http://localhost:9090/swagger-ui/index.html

You can add more sample data to the database by adding JSON files to the 'snapshot' folder and restarting the application.
Every time the application starts with local profile, it will clear the database and populate it again with the sample data if they are present.