pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh '/opt/homebrew/bin/mvn clean test'
            }
        }

        stage('Package') {
            steps {
                sh '/opt/homebrew/bin/mvn package -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'SocietyHub CI pipeline completed successfully.'
        }

        failure {
            echo 'SocietyHub CI pipeline failed.'
        }
    }
}