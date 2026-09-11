pipeline {
// W10: Jenkins continuous testing verification
    agent any

    environment {
        SOCIETYHUB_TEST_EMAIL = credentials('societyhub-test-user')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh '''
                    /opt/homebrew/bin/mvn clean test \
                        -Dtest.email="$SOCIETYHUB_TEST_EMAIL_USR" \
                        -Dtest.password="$SOCIETYHUB_TEST_EMAIL_PSW"
                '''
            }
        }

        stage('Package') {
            steps {
                sh '/opt/homebrew/bin/mvn package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    echo "Stopping existing SocietyHub instance..."

                    PID=$(lsof -tiTCP:8081 -sTCP:LISTEN || true)

                    if [ -n "$PID" ]; then
                        kill $PID
                        sleep 2
                    fi

                    echo "Starting SocietyHub..."

                    export JENKINS_NODE_COOKIE=dontKillMe

                    nohup /opt/homebrew/opt/openjdk@21/bin/java \
                        -jar target/society-facility-booking-portal-0.0.1-SNAPSHOT.jar \
                        --server.port=8081 \
                        > societyhub.log 2>&1 &

                    sleep 5

                    echo "Checking application..."

                    curl -f http://localhost:8081/facilities

                    echo "SocietyHub deployed successfully."
                '''
            }
        }
    }

    post {
        success {
            echo 'SocietyHub CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'SocietyHub CI/CD pipeline failed.'
        }
    }
}