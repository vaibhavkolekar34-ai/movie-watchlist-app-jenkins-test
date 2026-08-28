pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['TEST'], description: 'Deployment environment')
        string(name: 'TEST_SERVER', defaultValue: '', description: 'Private IP or DNS name of the test server')
        string(name: 'TEST_SERVER_USER', defaultValue: 'ec2-user', description: 'SSH user for EC2 #2')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn -B clean package'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit 'target/surefire-reports/*.xml'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/movie-watchlist.jar', fingerprint: true
            }
        }

        stage('Deploy to TEST') {
            when {
                expression { params.ENVIRONMENT == 'TEST' }
            }
            steps {
                sshagent(credentials: ['movie-watchlist-deploy']) {
                    sh '''
                        set -eu
                        SSH_OPTS="-o StrictHostKeyChecking=accept-new"
                        scp $SSH_OPTS target/movie-watchlist.jar "$TEST_SERVER_USER@$TEST_SERVER:/opt/movie-watchlist/movie-watchlist.jar"
                        ssh $SSH_OPTS "$TEST_SERVER_USER@$TEST_SERVER" "sudo systemctl restart movie-watchlist"
                    '''
                }
            }
        }

        stage('Health Check') {
            when {
                expression { params.ENVIRONMENT == 'TEST' }
            }
            steps {
                sshagent(credentials: ['movie-watchlist-deploy']) {
                    sh '''
                        set -eu
                        SSH_OPTS="-o StrictHostKeyChecking=accept-new"
                        for attempt in 1 2 3 4 5 6; do
                            if ssh $SSH_OPTS "$TEST_SERVER_USER@$TEST_SERVER" "curl --fail --silent http://localhost:8081/movies > /dev/null"; then
                                echo "Movie Watchlist health check passed."
                                exit 0
                            fi
                            sleep 5
                        done
                        echo "Movie Watchlist health check failed."
                        exit 1
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Movie Watchlist CI pipeline completed successfully.'
        }
        failure {
            echo 'Movie Watchlist CI pipeline failed. Review the console output and test results.'
        }
        always {
            echo "Build result: ${currentBuild.currentResult}"
        }
    }
}
