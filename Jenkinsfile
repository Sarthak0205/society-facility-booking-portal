pipeline {
    agent any

    environment {
        SOCIETYHUB_TEST_EMAIL = credentials('societyhub-test-user')
        PATH = "/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Docker Check') {
            steps {
                sh '''
                    echo "Docker path:"
                    which docker

                    echo "Docker version:"
                    /usr/local/bin/docker --version

                    echo "Docker containers:"
                    /usr/local/bin/docker ps
                '''
            }
        }

        stage('Package') {
            steps {
                sh '/opt/homebrew/bin/mvn package -DskipTests'
            }
        }

        stage('Start Application') {
            steps {
                sh '''
                    echo "Starting SocietyHub for automated testing..."

                    PID=$(lsof -tiTCP:8081 -sTCP:LISTEN || true)

                    if [ -n "$PID" ]; then
                        echo "Stopping existing SocietyHub instance..."
                        kill $PID
                        sleep 2
                    fi

                    export JENKINS_NODE_COOKIE=dontKillMe

                    nohup /opt/homebrew/opt/openjdk@21/bin/java \
                        -jar target/society-facility-booking-portal-0.0.1-SNAPSHOT.jar \
                        --server.port=8081 \
                        > societyhub.log 2>&1 &

                    echo "Waiting for SocietyHub to start..."

                    for i in {1..30}; do
                        if curl -s -f http://localhost:8081/facilities > /dev/null; then
                            echo "SocietyHub is running on port 8081."
                            exit 0
                        fi

                        sleep 1
                    done

                    echo "SocietyHub failed to start."
                    cat societyhub.log
                    exit 1
                '''
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

        stage('Deploy') {
            steps {
                sh '''
                    echo "SocietyHub deployment verified."

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