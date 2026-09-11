pipeline {
    agent any

    environment {
        SOCIETYHUB_TEST_EMAIL = credentials('societyhub-test-user')
        SOCIETYHUB_DB = credentials('societyhub-db-password')
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
                    echo "Docker version:"
                    docker --version

                    echo "Docker status:"
                    docker ps
                '''
            }
        }

        stage('Package') {
            steps {
                sh '''
                    /opt/homebrew/bin/mvn clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    echo "Building SocietyHub Docker image..."

                    docker build -t societyhub:${BUILD_NUMBER} .

                    echo "Docker image built successfully."

                    docker images societyhub
                '''
            }
        }

        stage('Docker Deploy') {
            steps {
                sh '''
                    echo "Deploying SocietyHub container..."

                    if docker ps -a --format '{{.Names}}' | grep -q '^societyhub$'; then
                        echo "Stopping existing SocietyHub container..."
                        docker stop societyhub || true

                        echo "Removing existing SocietyHub container..."
                        docker rm societyhub || true
                    fi

                    echo "Starting new SocietyHub container..."

                    docker run -d \
                        --name societyhub \
                        -p 8081:8081 \
                        -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/society_booking \
                        -e SPRING_DATASOURCE_USERNAME="$SOCIETYHUB_DB_USR" \
                        -e SPRING_DATASOURCE_PASSWORD="$SOCIETYHUB_DB_PSW" \
                        societyhub:${BUILD_NUMBER}

                    echo "Waiting for SocietyHub to start..."

                    for i in {1..30}; do
                        if curl -s -f http://localhost:8081/facilities > /dev/null; then
                            echo "SocietyHub Docker container is running."
                            exit 0
                        fi

                        sleep 1
                    done

                    echo "SocietyHub Docker container failed to start."
                    docker logs societyhub
                    exit 1
                '''
            }
        }

        stage('Test') {
            steps {
                sh '''
                    /opt/homebrew/bin/mvn test \
                        -Dtest.email="$SOCIETYHUB_TEST_EMAIL_USR" \
                        -Dtest.password="$SOCIETYHUB_TEST_EMAIL_PSW"
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "Verifying Docker deployment..."

                    docker ps

                    echo "Checking SocietyHub endpoint..."

                    curl -f http://localhost:8081/facilities

                    echo ""
                    echo "SocietyHub Docker deployment verified successfully."
                '''
            }
        }
    }

    post {
        success {
            echo 'SocietyHub Docker CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'SocietyHub Docker CI/CD pipeline failed.'
        }
    }
}