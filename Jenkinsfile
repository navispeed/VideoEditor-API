pipeline {
  agent any
  tools {
    maven 'maven3'
    jdk 'jdk14'
  }
  stages {
    stage('Initialize') {
      steps {
        sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -Dmaven.test.failure.ignore=true package'
      }
      post {
        success {
          junit 'target/surefire-reports/**/*.xml'
        }
      }
    }

    stage('Docker') {
      steps {
        sh 'cp target/*.jar docker/.'
        dir('docker') {
          script {
            def jarfiles = findFiles(glob: '*.jar')
            def img = docker.build('video-editor', "--build-arg JAR_FILE=${jarfiles[0].name} .")
            img.push()
          }
        }
      }
    }
  }
}
