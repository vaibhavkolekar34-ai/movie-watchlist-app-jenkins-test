pipeline {
    agent any

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
