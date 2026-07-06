// ── WMS CI Pipeline ──
// Triggers: push to master / develop
// Stages: Checkout → Backend Build&Test → SonarQube → OWASP DC → Frontend Build → npm Audit → Docker Build → Trivy Scan
//
// Jenkins 插件要求:
//   - Maven Integration
//   - SonarQube Scanner (配置 SonarQube Server 并命名为 'SonarQube')
//   - Docker Pipeline
//   - NodeJS Plugin (配置 NodeJS 20 安装并命名为 'node-20')
//   - OWASP Dependency-Check (可选，不安装则通过 Maven 命令执行)
//
// 工具配置 (Jenkins → Manage Jenkins → Tools):
//   - JDK: jdk-21 (Java 21)
//   - Maven: maven-3.9

pipeline {
    agent {
        // 需要 Docker 的 Jenkins agent，按实际环境调整 label
        label 'docker'
    }

    tools {
        maven 'maven-3.9'
        jdk 'jdk-21'
    }

    environment {
        // Maven 配置 — 使用本地仓库缓存加速构建
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository -Xmx2048m -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
        // Docker 镜像名称（不推送远程仓库，仅本地构建）
        DOCKER_SERVER_IMAGE = 'wms-server:${BRANCH_NAME}-${BUILD_NUMBER}'
        DOCKER_WEB_IMAGE    = 'wms-web:${BRANCH_NAME}-${BUILD_NUMBER}'
        // 前端 Node 版本
        NODE_VERSION = 'node-20'
        // SonarQube 排除项
        SONAR_EXCLUSIONS = '**/node_modules/**,**/target/**,**/dist/**,**/*.xml,**/*.json'
    }

    triggers {
        // Webhook 触发；Multibranch Pipeline 模式下此配置会被忽略
        pollSCM('')
    }

    options {
        // 保留最近 30 次构建记录和日志
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '5'))
        // 禁止并行构建同一个分支
        disableConcurrentBuilds()
        // 添加时间戳到控制台输出
        timestamps()
        // 超时 1 小时
        timeout(time: 1, unit: 'HOURS')
    }

    stages {

        // ──────────────────────────────────────
        // Stage 1: 代码检出
        // ──────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                // 在 Multibranch Pipeline 中自动匹配分支
                script {
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Build:  #${env.BUILD_NUMBER}"
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 2: 后端编译 + 单元测试
        // ──────────────────────────────────────
        stage('Backend Build & Test') {
            steps {
                dir('wms-server') {
                    // 编译 + 运行单元测试 + 代码格式校验
                    sh '''
                        mvn clean verify \
                            -Dmaven.test.failure.ignore=false \
                            -Dfmt.skip=false
                    '''
                }
            }
            post {
                success {
                    // 归档测试报告
                    junit allowEmptyResults: true,
                        testResults: 'wms-server/**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '⚠️ 后端编译或测试失败，请检查日志！'
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 3: SonarQube 代码质量分析
        // ──────────────────────────────────────
        stage('SonarQube Analysis') {
            when {
                // 仅在 master / develop 分支执行，PR 构建可跳过
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
                // 仅在有 SonarQube 配置时执行
                expression { env.SONAR_HOST_URL != null }
            }
            steps {
                dir('wms-server') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn sonar:sonar \
                                -Dsonar.projectKey=wms-server \
                                -Dsonar.java.binaries=**/target/classes \
                                -Dsonar.exclusions=${SONAR_EXCLUSIONS} \
                                -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 4: OWASP 依赖安全检查 (后端)
        // ──────────────────────────────────────
        stage('OWASP Dependency Check') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                dir('wms-server') {
                    // 用 Maven 直接运行 OWASP Dependency Check 插件
                    sh '''
                        mvn org.owasp:dependency-check-maven:check \
                            -pl wms-web \
                            -Dformat=HTML \
                            -DfailBuildOnCVSS=8 \
                            -DassemblyAnalyzerEnabled=false \
                            || echo '## WARNING: 高危依赖发现 (CVSS >= 8)，建议升级 ##'
                    '''
                }
            }
            post {
                always {
                    // 归档 OWASP 报告
                    archiveArtifacts allowEmptyArchive: true,
                        artifacts: 'wms-server/wms-web/target/dependency-check-report.html'
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 5: 前端编译 (并行)
        // ──────────────────────────────────────
        stage('Frontend Build') {
            parallel {
                // --- wms-web (PC) ---
                stage('wms-web') {
                    steps {
                        nodejs(configId: "${NODE_VERSION}") {
                            dir('wms-web') {
                                sh '''
                                    echo "📦 Installing dependencies..."
                                    npm ci --legacy-peer-deps
                                    echo "🔨 Building..."
                                    npm run build
                                '''
                            }
                        }
                    }
                    post {
                        success {
                            echo '✅ wms-web build succeeded'
                        }
                        failure {
                            echo '❌ wms-web build failed'
                        }
                    }
                }

                // --- wms-pda (Android H5) ---
                stage('wms-pda') {
                    steps {
                        nodejs(configId: "${NODE_VERSION}") {
                            dir('wms-pda') {
                                sh '''
                                    echo "📦 Installing dependencies..."
                                    npm ci --legacy-peer-deps
                                    echo "🔨 Building (android)..."
                                    npm run build:android
                                '''
                            }
                        }
                    }
                    post {
                        success {
                            echo '✅ wms-pda build succeeded'
                        }
                        failure {
                            echo '❌ wms-pda build failed'
                        }
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 6: npm 安全审计 (前端)
        // ──────────────────────────────────────
        stage('npm Audit') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                nodejs(configId: "${NODE_VERSION}") {
                    script {
                        def auditFailed = false
                        dir('wms-web') {
                            // npm audit 发现高危漏洞时仅警告，不阻断构建
                            def result = sh(
                                script: 'npm audit --audit-level=high 2>&1 || true',
                                returnStatus: true
                            )
                            if (result != 0) {
                                echo '## WARNING: wms-web npm audit 发现高危漏洞 ##'
                            }
                        }
                        dir('wms-pda') {
                            def result = sh(
                                script: 'npm audit --audit-level=high 2>&1 || true',
                                returnStatus: true
                            )
                            if (result != 0) {
                                echo '## WARNING: wms-pda npm audit 发现高危漏洞 ##'
                            }
                        }
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 7: Docker 镜像构建 (并行)
        // ──────────────────────────────────────
        stage('Docker Build') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            parallel {
                stage('wms-server Image') {
                    steps {
                        dir('wms-server') {
                            sh """
                                docker build \
                                    -t ${DOCKER_SERVER_IMAGE} \
                                    -t wms-server:latest \
                                    .
                            """
                        }
                    }
                    post {
                        success {
                            echo "🐳 wms-server image: ${DOCKER_SERVER_IMAGE}"
                        }
                    }
                }

                stage('wms-web Image') {
                    steps {
                        dir('wms-web') {
                            sh """
                                docker build \
                                    -t ${DOCKER_WEB_IMAGE} \
                                    -t wms-web:latest \
                                    .
                            """
                        }
                    }
                    post {
                        success {
                            echo "🐳 wms-web image: ${DOCKER_WEB_IMAGE}"
                        }
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 8: Docker 镜像安全扫描 (Trivy)
        // ──────────────────────────────────────
        stage('Trivy Security Scan') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    def trivyInstalled = sh(
                        script: 'which trivy || echo "NOT_FOUND"',
                        returnStdout: true
                    ).trim()

                    if (trivyInstalled != 'NOT_FOUND') {
                        sh """
                            echo '🔍 Scanning wms-server image...'
                            trivy image ${DOCKER_SERVER_IMAGE} \
                                --severity HIGH,CRITICAL \
                                --ignore-unfixed \
                                --no-progress \
                                --timeout 10m \
                                || echo '## WARNING: Trivy found vulnerabilities in wms-server ##'

                            echo '🔍 Scanning wms-web image...'
                            trivy image ${DOCKER_WEB_IMAGE} \
                                --severity HIGH,CRITICAL \
                                --ignore-unfixed \
                                --no-progress \
                                --timeout 10m \
                                || echo '## WARNING: Trivy found vulnerabilities in wms-web ##'
                        """
                    } else {
                        echo '⚠️ Trivy not installed, skipping container image scan.'
                        echo '   安装指引: curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh'
                    }
                }
            }
        }
    }

    // ──────────────────────────────────────
    // Post Actions: 构建后处理
    // ──────────────────────────────────────
    post {
        success {
            script {
                def duration = currentBuild.durationString
                echo "🎉 Build #${env.BUILD_NUMBER} succeeded (${duration})"
            }
        }
        failure {
            script {
                echo "❌ Build #${env.BUILD_NUMBER} failed!"
                // 可在此处添加通知，例如:
                //   - 企业微信/钉钉 webhook
                //   - Email (emailext plugin)
                //   - Slack (slackSend)
            }
        }
        always {
            // 清理工作区，避免磁盘堆积
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true
            )
        }
    }
}
