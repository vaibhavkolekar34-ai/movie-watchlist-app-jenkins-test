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

The pipeline can also deploy the packaged application to a separately prepared test server. Environment-specific values remain Jenkins parameters rather than being stored in this public repository.

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

## Deployment Pipeline

Install the Jenkins SSH Agent plugin and create a global SSH credential with ID `movie-watchlist-deploy`. The credential must contain the private key whose public key is installed in the test server user's `authorized_keys` file.

The test server must have Java 21, `/opt/movie-watchlist`, the `movie-watchlist.service` systemd unit, and passwordless permission for the deployment user to restart and check that service.

Start the job with **Build with Parameters** and provide:

- `ENVIRONMENT`: `TEST`
- `TEST_SERVER`: private IP or DNS name of the test server
- `TEST_SERVER_USER`: normally `ec2-user`

The pipeline copies the JAR to `/opt/movie-watchlist/movie-watchlist.jar`, restarts the service, and verifies `http://localhost:8081/movies` remotely. A failed deployment or health check fails the build.

## API

- `POST /movies` adds a movie. Body: `{ "title": "Inception", "genre": "Sci-Fi" }`
- `GET /movies` returns all movies.
- `PUT /movies/{id}/watched` marks a movie as watched.
- `PUT /movies/{id}/rating` rates a movie. Body: `{ "rating": 5 }`
