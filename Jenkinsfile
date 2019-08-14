#!/usr/bin/env groovy

pipeline {
    agent {
        label 'CLASSIC'
    }
    environment {
        commitReference = ''
        // NOTE : Those credentials need to be added to your jenkins instance's secrets manually before the first build.
        DB_IT = credentials('credentials-ara-db-it')                // User & Password of the Integration Database
        INTEGRATION_DB_URL = credentials('ara-integration-db-url')  // URL of the integration database (with jdbc prefix)
        DOCKER_REPO = credentials('docker-repo')                    // URL of your Docker repository to upload images
        SONAR_CREDENTIALS = credentials('quality_server')           // User & Password of your Sonar instance for Github
        SONAR_GITHUB_TOKEN = credentials('Jenkins_github_token')    // Github token for the communication with Sonar.
        NEXUS_RELEASE_REPO = credentials('nexus-release-repo')      // URL of your Maven Release repository.
        NEXUS_SNAPSHOTS_REPO = credentials('nexus-snapshot-repo')   // URL of your Maven Snapshot repository.
    }
    stages {
        stage('Pipeline Preparation') {
            steps {
                preparePipeline()
            }
        }
        stage('Build Server jar') {
            steps {
                buildProject()
                stash name: 'ara-jar', includes: "final/target/*.jar"
            }
        }
        stage('Build & Push server Image') {
            agent {
                label 'DOCKER'
            }
            steps {
                sh ('rm -rf final/target/*.jar')
                unstash 'ara-jar'
                mv final/target/*.jar final/ara.jar
                sh "docker build -t ${env.DOCKER_REPO}/ara-server:4.0.0-rc1 final/"
                sh "docker push ${env.DOCKER_REPO}/ara-server:4.0.0-rc1"
            }
        }
        stage('Build & Push db Image') {
            agent {
                label 'DOCKER'
            }
            steps {
                sh "docker build -t ${env.DOCKER_REPO}/ara-db:4.0.0-rc1 database/instance"
                sh "docker push ${env.DOCKER_REPO}/ara-db:4.0.0-rc1"
            }
        }
    }
}

def preparePipeline() {
    commitReference = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
    echo "commitReference=${commitReference}"
}

def buildProject() {
    def installOrDeploy = 'install'
    def dbUsr = "${env.DB_IT_USR}"
    def dbPsw = "${env.DB_IT_PSW}"
    def dbUrl = "${env.INTEGRATION_DB_URL}"
    def nexusReleases = "${env.NEXUS_RELEASE_REPO}"
    def nexusSnapshots = "${env.NEXUS_SNAPSHOTS_REPO}"

    maven {
        javaOpts = "-DaltReleaseDeploymentRepository=${nexusReleases} " +
                "-DaltSnapshotsDeploymentRepository=${nexusSnapshots} " +
                "-DaltDeploymentRepository=${nexusReleases} "
        goals = "clean ${installOrDeploy}"
    }
}