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
        stage('Build') {
            steps {
                buildProject()
                stash name: 'ara-jar', includes: "final/target/*.jar"
            }
        }
        stage('Quality Scan') {
            steps {
                qualityScan()
            }
        }
        stage('Build & Push Docker Images') {
            when {
                branch 'master'
            }
            agent {
                label 'DOCKER'
            }
            steps {
                sh ('rm -rf final/target/*.jar')
                unstash 'ara-jar'
                dir ('docker') {
                    sh "make push DOCKER_REGISTRY=${env.DOCKER_REPO}"
                }
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

    if (env.BRANCH_NAME == 'master') {
        installOrDeploy = 'deploy'
    }

    maven {
        javaOpts = "-DaltReleaseDeploymentRepository=${nexusReleases} " +
                "-DaltSnapshotsDeploymentRepository=${nexusSnapshots} " +
                "-DaltDeploymentRepository=${nexusReleases} " +
                '-Dliquibase.database.url="' + dbUrl +'" ' +
                "-Dliquibase.database.username=${dbUsr} " +
                "-Dliquibase.database.password=${dbPsw} " +
                '-Dspring.datasource.url="' + dbUrl + '" ' +
                "-Dspring.datasource.username=${dbUsr} " +
                "-Dspring.datasource.password=${dbPsw} "
        goals = "clean ${installOrDeploy}"
        profiles = 'in'
        testResults = '**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml'

    }
}


def qualityScan() {
    def prId = "${env.CHANGE_ID}"
    def sonarUser = "${env.SONAR_CREDENTIALS_USR}"
    def sonarPass = "${env.SONAR_CREDENTIALS_PSW}"
    def sonarGithubOauth = "${env.SONAR_GITHUB_TOKEN_PSW}"
    dir('server') {
        def sonarJavaOpts = "-Dsonar.branch=master -Dsonar.verbose=true -Dsonar.issuesReport.console.enable=true "
        if (env.BRANCH_NAME != 'master') {
            sonarJavaOpts = "${sonarJavaOpts} -Dsonar.analysis.mode=preview -Dsonar.github.pullRequest=${prId}  -Dsonar.github.repository=dktunited/ARA"
            sonarJavaOpts = "${sonarJavaOpts} -Dsonar.login=${sonarUser} -Dsonar.password=${sonarPass} -Dsonar.github.oauth=${sonarGithubOauth} "
        }
        sonarqubeAnalysis opts: sonarJavaOpts, useMaven: true
        if (env.BRANCH_NAME != 'master') {
            publishHTML([
                    reportDir            : 'target/sonar/issues-report',
                    reportFiles          : 'issues-report-light.html',
                    includes             : 'issues-report-light.html,issuesreport_files/**/*',
                    reportName           : 'SonarQube Reports',
                    reportTitles         : 'SonarQube Reports',
                    allowMissing         : true,
                    alwaysLinkToLastBuild: true,
                    keepAll              : true

            ])
        }
    }
}
