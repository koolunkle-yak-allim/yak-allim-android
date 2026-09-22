pipeline {
    agent any

    options {
        // 배포 파이프라인이 동시에 여러 개 겹쳐 돌면 서버 쪽 블루-그린 전환과
        // 마찬가지로 자격 증명 파일 복사/빌드 산출물이 서로 덮어쓸 수 있어 직렬화한다.
        disableConcurrentBuilds()
    }

    // Slack 알림 연동 환경 변수
    environment {
        SLACK_CREDENTIAL_ID = 'slack-bot-token'
        SLACK_CHANNEL       = '#app-deploy-alerts'
    }

    parameters {
        booleanParam(
            name: 'CLEAN_BUILD',
            defaultValue: false,
            description: '선택 시 기존 빌드 캐시를 삭제하고 클린 빌드를 수행합니다.'
        )
        choice(
            name: 'BUILD_TYPE',
            choices: ['assembleDebug', 'assembleRelease'],
            description: '빌드 대상 타입을 선택합니다 (Debug / Release).'
        )
    }

    stages {
        stage('Build Android APK') {
            steps {
                // 보안 자격 증명 주입 및 빌드
                withCredentials([
                        file(credentialsId: 'android-secrets-properties', variable: 'SECRETS_PROPERTIES'),
                        file(credentialsId: 'android-google-services-json', variable: 'GOOGLE_SERVICES_JSON')
                    ]) {
                    script {
                        def cleanOption = params.CLEAN_BUILD ? 'clean' : ''
                        def buildTask = params.BUILD_TYPE ?: 'assembleDebug'

                        if (isUnix()) {
                            // 1. 자격 증명 파일 workspace 복사 및 실행 권한 부여
                            sh """
                                rm -f secrets.properties app/google-services.json
                                cp -f "\$SECRETS_PROPERTIES" secrets.properties
                                cp -f "\$GOOGLE_SERVICES_JSON" app/google-services.json
                                chmod +x gradlew
                            """

                            // 2. Dynamic Docker Container Agent: --volumes-from으로 Jenkins 볼륨을 공유하여 빌드 진행 후 자동 파기
                            sh """
                                docker run --rm \
                                    --volumes-from yak-allim-jenkins \
                                    -v yak-allim-android-gradle-cache:/root/.gradle \
                                    -w "${env.WORKSPACE}" \
                                    thyrlian/android-sdk:10.0 \
                                    sh -c "./gradlew ${cleanOption} ${buildTask}"
                            """
                        } else {
                            withEnv([
                                    "ANDROID_HOME=${env.ANDROID_HOME ?: 'C:\\Users\\a4336\\AppData\\Local\\Android\\Sdk'}",
                                    "JAVA_HOME=${env.JAVA_HOME ?: 'C:\\Program Files\\Java\\jdk-17'}"
                                ]) {
                                bat """
                                    if exist secrets.properties del /f /q secrets.properties
                                    if exist app\\google-services.json del /f /q app\\google-services.json
                                    copy /y "%SECRETS_PROPERTIES%" "secrets.properties"
                                    copy /y "%GOOGLE_SERVICES_JSON%" "app\\google-services.json"

                                    if not exist local.properties (
                                        if defined ANDROID_HOME (
                                            echo sdk.dir=%ANDROID_HOME% > local.properties
                                        )
                                    )

                                    gradlew.bat ${cleanOption} ${buildTask}
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                // 생성된 APK 결과물 아카이빙
                archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk', allowEmptyArchive: false
            }
        }
    }

    // 파이프라인 빌드 결과에 따른 Slack 봇 알림 전송
    post {
        // 빌드 성공 알림
        success {
            script {
                try {
                    def channel = env.SLACK_CHANNEL ?: '#app-deploy-alerts'
                    def credId  = env.SLACK_CREDENTIAL_ID ?: 'slack-bot-token'

                    def successMessage = """
                        *:white_check_mark: [SUCCESS] Android APK Build Completed*
                        • *Job:* `${env.JOB_NAME}`
                        • *Build Number:* #${env.BUILD_NUMBER}
                        • *Duration:* ${currentBuild.durationString}
                        • *Link:* <${env.BUILD_URL}|Open Build> | <${env.BUILD_URL}console|Console Log>
                    """.stripIndent().trim()

                    slackSend botUser: true, color: '#36a64f', channel: channel, tokenCredentialId: credId, message: successMessage
                } catch (Exception e) {
                    echo "Slack 알림 전송 건너뜀 (사유: ${e.message})"
                }
            }
        }

        // 빌드 실패 알림
        failure {
            script {
                try {
                    def channel = env.SLACK_CHANNEL ?: '#app-deploy-alerts'
                    def credId  = env.SLACK_CREDENTIAL_ID ?: 'slack-bot-token'

                    def failureMessage = """
                        *:x: [FAILURE] Android APK Build Failed*
                        • *Job:* `${env.JOB_NAME}`
                        • *Build Number:* #${env.BUILD_NUMBER}
                        • *Duration:* ${currentBuild.durationString}
                        • *Build Link:* <${env.BUILD_URL}|Open Build>
                        • *Failed Console Log:* <${env.BUILD_URL}console|View Logs>

                        *Check Logs:*
                        실패한 빌드의 상세 에러 원인은 위 Console Log 링크에서 확인하실 수 있습니다.
                    """.stripIndent().trim()

                    slackSend botUser: true, color: '#FF0000', channel: channel, tokenCredentialId: credId, message: failureMessage
                } catch (Exception e) {
                    echo "Slack 알림 전송 건너뜀 (사유: ${e.message})"
                }
            }
        }
    }
}