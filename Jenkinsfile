// ── WMS CI/CD Pipeline ──
// 触发: 任务(ml-wms)上配置的 SCM 轮询
// 阶段: Checkout → 后端构建&测试 → SonarQube → OWASP → 前端构建 → npm Audit → 部署到 tencent-test
//
// 与 Jenkins 实际环境的对齐说明（改动前请先核对）:
//   - Jenkins 容器自带 Temurin JDK 21 → 不声明 jdk 工具
//   - Maven 全局工具的实际安装名是 "maven"
//   - NodeJS 需在 Manage Jenkins → Tools 中新增安装名 "node-20"
//   - 部署通过已配置的 Publish over SSH 主机 "tencent-test" 完成
//   - 本流水线不在 Jenkins 侧构建镜像: 该 Jenkins 容器没有可用的 docker daemon
//     （DOCKER_HOST=tcp://docker:2376 指向不存在的 dind），镜像在目标服务器上构建
//
// Jenkins 插件要求:
//   - Maven Integration / NodeJS / SonarQube Scanner
//   - Publish over SSH（已配置主机 tencent-test）
//   - Workspace Cleanup（cleanWs）

pipeline {
    // 当前 Jenkins 只有内置控制器一个执行器，没有任何带 label 的 agent
    agent any

    tools {
        maven 'maven'
    }

    environment {
        // 使用容器内 ~/.m2（持久化在 jenkins-data 卷）；
        // 不要再指定 -Dmaven.repo.local=.m2/repository —— 工作区每次构建都会被 cleanWs 清掉，
        // 等于每次都重新下载全部依赖。
        MAVEN_OPTS = '-Xmx2048m -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
        // NodeJS 工具安装名（需在 Jenkins 全局工具中配置）
        NODE_VERSION = 'node-20'
        // SonarQube 排除项
        SONAR_EXCLUSIONS = '**/node_modules/**,**/target/**,**/dist/**,**/*.xml,**/*.json'
        // 部署目标
        DEPLOY_HOST = 'tencent-test'
        DEPLOY_HOME = '/opt/wms'
    }

    triggers {
        // 显式声明每分钟轮询。
        //
        // 为什么必须在这里声明：任务上原有的 SCMTrigger 实际没有绑定到 Git SCM，每次轮询都是
        //   "Done. Took 0 ms / No changes"（连 git ls-remote 都没执行），因此永远不会自动触发。
        // 对比同环境下正常的任务："> git ls-remote ... / Done. Took 0.74 sec"。
        //
        // 在 Jenkinsfile 中声明 triggers，构建时会通过 properties() 重新注册触发器并绑定当前 SCM，
        // 所以手动触发一次之后即可自愈。注意切勿写成 pollSCM('') —— 空 cron 等于关闭轮询。
        pollSCM('* * * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '5'))
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }

    stages {

        // ──────────────────────────────────────
        // Stage 1: 代码检出
        // ──────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // 普通 Pipeline 任务不会设置 BRANCH_NAME，用 GIT_BRANCH 兜底
                    echo "Branch: ${env.GIT_BRANCH ?: env.BRANCH_NAME ?: 'unknown'}"
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
                    // 必须 install（而不是 verify）:
                    // 后面的 OWASP 阶段用 -pl 单模块构建，兄弟模块要从本地仓库解析。
                    sh '''
                        mvn clean install \
                            -Dmaven.test.failure.ignore=false \
                            -Dfmt.skip=false
                    '''
                }
            }
            post {
                always {
                    // 放在 always: 测试失败时更需要对报告
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
                // 注意: 这是普通 Pipeline 任务（非 Multibranch），env.BRANCH_NAME 为空，
                // when { branch 'master' } 永远不成立 → 会导致整个阶段被静默跳过。
                // 任务本身已限定只构建 master，这里只保留"是否配置了 SonarQube"的判断。
                expression { env.SONAR_HOST_URL != null }
            }
            steps {
                dir('wms-server') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn sonar:sonar \
                                -Dsonar.projectKey=wms-server \
                                -Dsonar.java.binaries=**/target/classes \
                                -Dsonar.exclusions=${SONAR_EXCLUSIONS}
                        '''
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 4: OWASP 依赖安全检查（后端）
        // ──────────────────────────────────────
        stage('OWASP Dependency Check') {
            steps {
                dir('wms-server') {
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
                    archiveArtifacts allowEmptyArchive: true,
                        artifacts: 'wms-server/wms-web/target/dependency-check-report.html'
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 5: 前端编译（并行）
        // ──────────────────────────────────────
        stage('Frontend Build') {
            parallel {
                // --- wms-web (PC) ---
                stage('wms-web') {
                    steps {
                        nodejs(nodeJSInstallationName: "${NODE_VERSION}") {
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
                        nodejs(nodeJSInstallationName: "${NODE_VERSION}") {
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
        // Stage 6: npm 安全审计（前端）
        // ──────────────────────────────────────
        stage('npm Audit') {
            steps {
                nodejs(nodeJSInstallationName: "${NODE_VERSION}") {
                    script {
                        dir('wms-web') {
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
        // Stage 7: 部署到 tencent-test
        //   传输后端 fat jar + 前端 dist + 部署脚本，
        //   由目标服务器执行 docker compose build & up（服务器上有可用的 docker daemon）
        // ──────────────────────────────────────
        stage('Deploy to tencent-test') {
            options {
                timeout(time: 15, unit: 'MINUTES')
            }
            steps {
                echo "🚀 发布到 ${DEPLOY_HOST} (build #${env.BUILD_NUMBER})"

                // 1) 在工作区组装发布包。
                //    只传一个 tar 包，规避 Publish over SSH 的目录语义：
                //      - 主机的 Remote Directory 留空时，所有路径都相对 SSH 用户家目录（/root）
                //      - remoteDirectory 的前导 "/" 会被剥掉
                //      - sourceFiles 带目录时，会在远端重现该目录层级
                sh '''
                    rm -rf .deploy-staging
                    mkdir -p .deploy-staging/server .deploy-staging/web

                    cp deploy/docker-compose.yml .deploy-staging/
                    cp deploy/deploy.sh           .deploy-staging/
                    cp wms-server/wms-web/src/main/resources/db/init.sql .deploy-staging/

                    cp deploy/server/Dockerfile .deploy-staging/server/
                    NEWEST_JAR=$(ls -1t wms-server/wms-web/target/wms-web-*.jar | head -1)
                    cp "$NEWEST_JAR" .deploy-staging/server/

                    cp deploy/web/Dockerfile .deploy-staging/web/
                    cp deploy/web/nginx.conf .deploy-staging/web/
                    cp -r wms-web/dist       .deploy-staging/web/dist

                    tar czf wms-deploy.tar.gz -C .deploy-staging .
                    echo "发布包内容:"
                    tar tzf wms-deploy.tar.gz | head -20
                    ls -lh wms-deploy.tar.gz
                '''

                // 2) 传输发布包 → 解包 → 执行部署脚本。
                //    解包命令兼容三种落点：已按推荐配置 Remote Directory=/opt/wms，
                //    或未配置（落到 /root/opt/wms 或 /root）。
                sshPublisher(
                    failOnError: true,
                    publishers: [
                        sshPublisherDesc(
                            configName: "${DEPLOY_HOST}",
                            verbose: true,
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'wms-deploy.tar.gz',
                                    remoteDirectory: "${DEPLOY_HOME}",
                                    flatten: true,
                                    execCommand: "mkdir -p ${DEPLOY_HOME} && (tar xzf ${DEPLOY_HOME}/wms-deploy.tar.gz -C ${DEPLOY_HOME} || tar xzf /root${DEPLOY_HOME}/wms-deploy.tar.gz -C ${DEPLOY_HOME} || tar xzf /root/wms-deploy.tar.gz -C ${DEPLOY_HOME}) && sh ${DEPLOY_HOME}/deploy.sh"
                                )
                            ]
                        )
                    ]
                )
            }
        }
    }

    // ──────────────────────────────────────
    // Post Actions
    // ──────────────────────────────────────
    post {
        success {
            script {
                echo "🎉 Build #${env.BUILD_NUMBER} succeeded (${currentBuild.durationString})"
            }
        }
        failure {
            echo "❌ Build #${env.BUILD_NUMBER} failed!"
            // 可在此处补充通知:
            //   - 企业微信/钉钉 webhook
            //   - Email (emailext plugin)
            //   - Slack (slackSend)
        }
        always {
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true
            )
        }
    }
}
