# Movie Watchlist App with Jenkins CI/CD

This project demonstrates CI and test-environment deployment for a small Spring Boot Movie Watchlist application.

## Scope

The pipeline performs:

1. Source checkout from the configured Git repository
2. Maven compilation and packaging
3. JUnit test execution
4. JUnit result publication in Jenkins
5. JAR artifact archiving with a fingerprint
6. Parameterized deployment to a test EC2 instance
7. Application health verification
8. Success or failure reporting in the Jenkins console

The same pipeline supports CI-only and CI/CD runs. Environment-specific values remain Jenkins parameters rather than being stored in this public repository.

## Requirements

- Java 21
- Maven 3.8 or later
- Git
- Jenkins with Pipeline, Git, JUnit, Credentials, and SSH Agent plugins
- Two reachable Amazon Linux EC2 instances: Jenkins server and test server

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

Jenkins runs on the controller EC2 instance at `http://<JENKINS_PUBLIC_IP>:8070`. Allow inbound TCP port `8070` only from the addresses that need UI access.

1. Create a Pipeline job named `movie-watchlist-ci`.
2. Select **Pipeline script from SCM**.
3. Select Git and enter `https://github.com/vaibhavkolekar34-ai/movie-watchlist-app-jenkins-test.git`.
4. Set the branch to `*/main` and the script path to `Jenkinsfile`.
5. Save the job and run it once so Jenkins loads the parameters.

When using an inline **Pipeline script** instead, `checkout scm` is unavailable. Replace the checkout step with:

```groovy
git branch: 'main',
	url: 'https://github.com/vaibhavkolekar34-ai/movie-watchlist-app-jenkins-test.git'
```

The `Build and Test` stage runs `mvn -B clean package`. Maven fails the stage when compilation or a unit test fails, so the later publication and archive stages are not run. Successful builds archive `target/movie-watchlist.jar`.

## Deployment Pipeline

Install the Jenkins SSH Agent plugin and create a global **SSH Username with private key** credential:

```text
ID: movie-watchlist-deploy
Username: ec2-user
```

The corresponding public key must be present in `/home/ec2-user/.ssh/authorized_keys` on the test server. Allow inbound TCP port `22` on the test server from the Jenkins EC2 security group.

The test server requires Java 21 and an application directory owned by `ec2-user`:

```bash
sudo dnf install -y java-21-amazon-corretto-devel curl
sudo mkdir -p /opt/movie-watchlist
sudo chown -R ec2-user:ec2-user /opt/movie-watchlist
```

Create `/etc/systemd/system/movie-watchlist.service`:

```ini
[Unit]
Description=Movie Watchlist Application
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/movie-watchlist
ExecStart=/usr/bin/java -jar /opt/movie-watchlist/movie-watchlist.jar --server.port=8081
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable the service and permit `ec2-user` to restart it without a password:

```bash
sudo systemctl daemon-reload
sudo systemctl enable movie-watchlist
echo 'ec2-user ALL=(root) NOPASSWD: /usr/bin/systemctl restart movie-watchlist, /usr/bin/systemctl status movie-watchlist' | sudo tee /etc/sudoers.d/movie-watchlist
sudo chmod 440 /etc/sudoers.d/movie-watchlist
sudo visudo -c
```

## Running the Pipeline

For a CI-only run, select **Build with Parameters** and use:

```text
ENVIRONMENT: NONE
TEST_SERVER: leave blank
TEST_SERVER_USER: ec2-user
```

The deployment and health-check stages are skipped.

For a test deployment, use:

```text
ENVIRONMENT: TEST
TEST_SERVER: private IP or DNS name of the test server
TEST_SERVER_USER: ec2-user
```

The pipeline copies the JAR to `/opt/movie-watchlist/movie-watchlist.jar`, restarts the service, and verifies `http://localhost:8081/movies` remotely. A failed deployment or health check fails the build.

## Build Triggers

To trigger builds from GitHub pushes:

1. Enable **GitHub hook trigger for GITScm polling** in the Jenkins job.
2. Add a GitHub webhook with payload URL `http://<JENKINS_PUBLIC_IP>:8070/github-webhook/`.
3. Select `application/json`, **Just the push event**, and keep the webhook active.
4. Confirm a recent delivery returns HTTP `200`, then push a commit to the branch configured in the Jenkins checkout.

Webhook builds use parameter defaults. `ENVIRONMENT=NONE` is the default so an ordinary source push performs CI without an unintended deployment.

To schedule the job, enable **Build periodically** and enter:

```text
H 22 * * *
```

This schedules one daily CI run around 10 PM in the Jenkins controller's timezone. Scheduled builds also use the default CI-only parameters.

## Monitoring and Verification

Use Jenkins **Build History**, **Pipeline Overview**, **Console Output**, **Tests**, archived artifacts, fingerprints, build status, and duration for basic monitoring.

Verify the deployed service on the test server:

```bash
sudo systemctl status movie-watchlist --no-pager
sudo journalctl -u movie-watchlist -n 50 --no-pager
curl -i http://localhost:8081/movies
```

A successful health request returns HTTP `200`; an empty in-memory watchlist returns `[]`.

## API

- `POST /movies` adds a movie. Body: `{ "title": "Inception", "genre": "Sci-Fi" }`
- `GET /movies` returns all movies.
- `PUT /movies/{id}/watched` marks a movie as watched.
- `PUT /movies/{id}/rating` rates a movie. Body: `{ "rating": 5 }`
