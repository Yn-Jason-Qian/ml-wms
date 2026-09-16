// ── WMS CI/CD Pipeline ──
// 触发: 任务上配置的 SCM 轮询（见下方 triggers）
// 阶段: Checkout → 后端构建&测试 → SonarQube → 前端构建 → npm Audit → 部署(可选)
//
// 说明: 环境相关内容（主机名、路径、口令）一律放在 Jenkins 侧，仓库里只保留通用逻辑。
//
//   - Jenkins 容器自带 Temurin JDK 21 → 不声明 jdk 工具
//   - Maven 全局工具安装名: maven
//   - NodeJS 全局工具安装名: node-20（Manage Jenkins → Tools 中新增）
//   - 部署目标由全局环境变量提供
//     （Manage Jenkins → System → Global properties → Environment variables）:
//         WMS_DEPLOY_HOST = 在 Publish over SSH 里配置的主机名；未配置时部署阶段自动跳过
//         WMS_DEPLOY_HOME = 远端部署目录，默认 /opt/wms
//   - 部署阶段只在 wms-server/、wms-web/、deploy/ 下有文件改动时执行
//     （手动 Build Now 总是执行）；纯文档、CI 配置、wms-pda 改动不重新部署
//   - 部署范围同样按变更路径收窄：只改了 wms-web/ 就只传 dist、只重建 web 容器，
//     不再为前端改动重传 ~100MB 的后端 fat jar
//   - 本流水线不在 Jenkins 侧构建镜像（Jenkins 容器通常没有可用的 docker daemon），
//     镜像在目标服务器上由 deploy/deploy.sh 执行 docker compose build 生成
//   - 镜像 tag 用 b<构建号>-<短 commit>（不是 latest），远端 deploy.sh 会记录发布顺序，
//     健康检查失败自动回滚，需要时可在目标服务器执行 deploy/rollback.sh 手工回滚
//
// Jenkins 插件要求:
//   - Maven Integration / NodeJS / SonarQube Scanner
//   - Publish over SSH（仅在需要自动部署时）
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
        // 部署目标：从 Jenkins 全局环境变量读取，仓库里不写死具体主机/路径。
        // 未配置 WMS_DEPLOY_HOST 时（例如刚 fork 的人），Stage 7 会自动跳过。
        DEPLOY_HOST = "${env.WMS_DEPLOY_HOST ?: ''}"
        DEPLOY_HOME = "${env.WMS_DEPLOY_HOME ?: '/opt/wms'}"
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

                    // 发布标识：写进镜像 tag 与远端发布记录，回滚时按它选版本
                    env.WMS_RELEASE_ID = "b${env.BUILD_NUMBER}-${(env.GIT_COMMIT ?: 'nogit').take(7)}"

                    // 变更路径与发布范围在这里算一次，Deploy 阶段的条件和打包步骤共用同一份结论，
                    // 避免两处判断逻辑各写一遍、结果对不上。
                    def paths = wmsChangedPaths()
                    def comps
                    if ((env.DEPLOY_HOST ?: '').trim() == '') {
                        // 未配置部署目标（例如别人 fork 了仓库）→ 跳过部署
                        comps = []
                        echo '未配置 WMS_DEPLOY_HOST，Deploy 阶段将跳过'
                    } else if (currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause')) {
                        // 手动触发（Build Now）→ 总是全量重建，方便随时"就地重发"
                        comps = ['server', 'web']
                    } else if (paths == null || paths.isEmpty()) {
                        // 拿不到变更信息（例如首次构建）→ 保守处理：全部重建
                        comps = ['server', 'web']
                    } else {
                        comps = wmsDeployComponents(paths)
                    }
                    env.WMS_DEPLOY_COMPONENTS = comps.join(',')
                    env.WMS_SHOULD_DEPLOY = (comps.isEmpty() ? 'false' : 'true')
                    if (comps.isEmpty()) {
                        echo '本次改动不涉及发布产物，Deploy 阶段将跳过'
                    } else {
                        echo "发布标识 ${env.WMS_RELEASE_ID}，重建范围: ${env.WMS_DEPLOY_COMPONENTS}"
                    }
                }
            }
        }

        // ──────────────────────────────────────
        // Stage 2: 后端编译 + 单元测试
        // ──────────────────────────────────────
        stage('Backend Build & Test') {
            steps {
                dir('wms-server') {
                    // 用 install 而非 verify：跑完单元测试与格式校验后，
                    // 顺带把各模块装进本地 Maven 仓库，方便后续阶段或本地复跑时按单模块（-pl）构建。
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
        // Stage 4: 前端编译（并行）
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
        // Stage 5: npm 安全审计（前端）
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
        // Stage 6: 部署（可选）
        //   传输后端 fat jar + 前端 dist + 部署脚本，
        //   由目标服务器执行 docker compose build & up（服务器上需有可用的 docker daemon）。
        //   每次发布一个独立镜像 tag（b<构建号>-<短 commit>），旧镜像留在目标服务器上，
        //   健康检查失败时 deploy.sh 会自动回滚到上一个发布 —— 详见 deploy/README.md。
        //   未配置 WMS_DEPLOY_HOST 时本阶段跳过，其余阶段不受影响 —— fork 后开箱即可通过构建。
        // ──────────────────────────────────────
        stage('Deploy') {
            when {
                // 判断逻辑（部署目标是否配置、变更路径是否涉及发布产物）在 Stage 1 里完成，
                // 结论通过 env.WMS_SHOULD_DEPLOY 带过来，与打包步骤共用同一份结果。
                expression { env.WMS_SHOULD_DEPLOY == 'true' }
            }
            options {
                timeout(time: 15, unit: 'MINUTES')
            }
            steps {
                echo "🚀 发布到 ${DEPLOY_HOST} (build #${env.BUILD_NUMBER}, release ${env.WMS_RELEASE_ID}, 重建 ${env.WMS_DEPLOY_COMPONENTS})"

                // 1) 在工作区组装发布包。
                //    只传一个 tar 包，规避 Publish over SSH 的目录语义：
                //      - 主机的 Remote Directory 留空时，所有路径都相对 SSH 用户家目录（/root）
                //      - remoteDirectory 的前导 "/" 会被剥掉
                //      - sourceFiles 带目录时，会在远端重现该目录层级
                //    只放「本次真正重建的服务」的产物：纯前端改动不必再传 ~100MB 的 fat jar。
                sh '''
                    rm -rf .deploy-staging
                    mkdir -p .deploy-staging/server .deploy-staging/web

                    cp deploy/docker-compose.yml .deploy-staging/
                    cp deploy/deploy.sh           .deploy-staging/
                    cp deploy/rollback.sh         .deploy-staging/
                    cp deploy/lib.sh              .deploy-staging/
                    cp deploy/.env.example        .deploy-staging/
                    cp wms-server/wms-web/src/main/resources/db/init.sql .deploy-staging/

                    cp deploy/server/Dockerfile .deploy-staging/server/
                    cp deploy/web/Dockerfile    .deploy-staging/web/
                    cp deploy/web/nginx.conf    .deploy-staging/web/
                '''
                script {
                    def components = (env.WMS_DEPLOY_COMPONENTS ?: 'server,web').split(',') as List

                    if (components.contains('server')) {
                        sh 'cp "$(ls -1t wms-server/wms-web/target/wms-web-*.jar | head -1)" .deploy-staging/server/'
                    } else {
                        echo '本次不重建后端：跳过 jar 传输'
                    }
                    if (components.contains('web')) {
                        sh 'cp -r wms-web/dist .deploy-staging/web/dist'
                    } else {
                        echo '本次不重建前端：跳过 dist 传输'
                    }

                    // 远端 deploy.sh 会把它记进发布状态，方便回滚时看清是哪个 commit
                    writeFile file: '.deploy-staging/release.env', text: """WMS_RELEASE=${env.WMS_RELEASE_ID}
WMS_GIT_SHA=${env.GIT_COMMIT ?: ''}
WMS_BUILD_NUMBER=${env.BUILD_NUMBER}
WMS_COMPONENTS=${components.join(',')}
"""
                }
                sh '''
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
                                    execCommand: "mkdir -p ${DEPLOY_HOME} && (tar xzf ${DEPLOY_HOME}/wms-deploy.tar.gz -C ${DEPLOY_HOME} || tar xzf /root${DEPLOY_HOME}/wms-deploy.tar.gz -C ${DEPLOY_HOME} || tar xzf /root/wms-deploy.tar.gz -C ${DEPLOY_HOME}) && sh ${DEPLOY_HOME}/deploy.sh --release ${env.WMS_RELEASE_ID} --components ${env.WMS_DEPLOY_COMPONENTS}"
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
                notFailBuild: true,
                // 保留 .git：否则每次构建都要把整个仓库重新 clone 一遍（约 50MB 的 pack），
                // 到 GitHub 网络不稳时会直接卡死/失败（构建 #33 就是死在 clone 上）。
                // 保留后每次只做增量 fetch，构建更快也更抗网络抖动。
                // 编译产物（target/、node_modules/、dist/）仍然每次清掉，不受影响。
                patterns: [[pattern: '**/.git/**', type: 'EXCLUDE']]
            )
        }
    }
}

// ──────────────────────────────────────
// 辅助方法（供 Stage 1 与 Deploy 阶段共用）
// ──────────────────────────────────────

// 本次构建涉及的文件路径，统一成带前导 '/' 的形式，便于用 contains 匹配目录段。
// 返回 null 表示拿不到变更信息（调用方按「全部重建」处理）。
def wmsChangedPaths() {
    try {
        def changed = []
        currentBuild.changeSets.each { set ->
            set.items.each { item ->
                item.affectedFiles.each { f ->
                    changed << ('/' + f.path.replace('\\', '/'))
                }
            }
        }
        return changed
    } catch (Throwable ignored) {
        return null
    }
}

// 由变更路径推出需要重建的服务（server / web）。
//
// 这里刻意不用声明式的 changeset 条件：它的通配语义不是 Ant 风格，
// 'wms-server/**' 匹配不到 'wms-server/wms-common/...' 这类深层文件，
// 实测构建 #28 对本应部署的后端改动也跳过了（静默漏部署，比多部署更糟）。
// 自己比对路径语义明确，且拿不到变更信息时按「全部重建」处理（失败偏安全）。
def wmsDeployComponents(List paths) {
    def comps = []
    if (paths.any { it.contains('/wms-server/') }) {
        comps << 'server'
    }
    if (paths.any { it.contains('/wms-web/') }) {
        comps << 'web'
    }
    // deploy/ 下是编排文件、Dockerfile、nginx 配置与部署脚本：两个服务都可能受影响
    if (paths.any { it.contains('/deploy/') }) {
        ['server', 'web'].each { svc -> if (!comps.contains(svc)) { comps << svc } }
    }
    return comps
}
