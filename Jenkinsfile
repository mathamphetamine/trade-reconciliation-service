pipeline {
    agent any
    
    tools {
        jdk 'jdk17'
        maven 'maven3'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'mvn verify -DskipUnitTests'
                }
            }
            post {
                always {
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'docker build -t trade-reconciliation-service:${BUILD_NUMBER} .'
                    sh 'docker tag trade-reconciliation-service:${BUILD_NUMBER} trade-reconciliation-service:latest'
                }
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                dir('trade-reconciliation-service') {
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build and deployment successful!'
        }
        failure {
            echo 'Build or deployment failed!'
        }
    }
} 