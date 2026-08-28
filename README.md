# Movie Watchlist App with Jenkins CI

This project demonstrates a Jenkins CI pipeline for a small Spring Boot Movie Watchlist application.

## Scope

The pipeline performs:

1. Source checkout from the configured Git repository
2. Maven compilation and packaging
3. JUnit test execution
4. JUnit result publication in Jenkins
5. JAR artifact archiving with a fingerprint
6. Success or failure notification in the Jenkins console

Deployment and environment-specific infrastructure are intentionally outside this application repository.

## Requirements

- Java 21
- Maven 3.9 or later
- Jenkins with Pipeline, Git, and JUnit support

## Run Locally

From this directory:

```bash
mvn clean package
```

The packaged artifact is created at:

```text
target/movie-watchlist.jar
```

Run the application with:

```bash
java -jar target/movie-watchlist.jar
```

The in-memory API is available at `http://localhost:8080/movies`.

## Jenkins Configuration

1. Create a Pipeline job.
2. Configure the job to use `Pipeline script from SCM`.
3. Select Git and provide the repository URL and credentials if the repository is private.
4. Set the script path to `Jenkinsfile`.
5. Run the job.

The `Build and Test` stage runs `mvn -B clean package`. Maven fails the stage when compilation or a unit test fails, so the later publication and archive stages are not run. Successful builds archive `target/movie-watchlist.jar`.

## API

- `POST /movies` adds a movie. Body: `{ "title": "Inception", "genre": "Sci-Fi" }`
- `GET /movies` returns all movies.
- `PUT /movies/{id}/watched` marks a movie as watched.
- `PUT /movies/{id}/rating` rates a movie. Body: `{ "rating": 5 }`
