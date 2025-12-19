// vars/backendEngine.groovy
def call(String AGENT = 'agents/default.yaml') {
    pipeline {
        agent {
            kubernetes {
                yaml libraryResource(AGENT)
            }
        }
        parameters {
        string(name: 'ENV', defaultValue: 'demo', description: 'Env to deploy on')
        string(name: 'REPO_URL', defaultValue: 'https://github.com/MostafaAnas/go-cli-todo', description: 'GitHub repository URL to scan')
        string(name: 'OLLAMA_MODEL', defaultValue: 'llama3.2:1b', description: 'Ollama model to use')
        string(name: 'OLLAMA_SERVER_URL', defaultValue: 'http://172.22.24.2:8080', description: 'Ollama server URL')
        string(name: 'OUTPUT_FILE', defaultValue: '', description: 'Output file name (optional)')
        string(name: 'REPONAME', defaultValue: 'https://github.com/MostafaAnas/go-cli-todo', description: 'Repository name for DAST(e.g., org/repo or repo)')
        booleanParam(name: 'RUN_RAISEPR', defaultValue: false, description: 'Tick to run RaisePR stage')

        }
        environment {
            DAST_API = credentials('DAST-API')
        }

        stages {
            stage('Init'){
                steps{
                    script{
                        
                        echo "Full repository URL: ${env.GIT_URL}"
                        def repoName = env.GIT_URL.tokenize('/').last().replaceAll(/\.git$/, '')
                        echo "Repository name: ${repoName}"
                        env.repoName = repoName.toLowerCase()
                        echo "Repository name: ${env.repoName}"
                    }
                }
            }
            stage('buildpy') {
                steps {
                    script {
                        container('buildpy') {
                            buildpy()
                        }
                    }
                }
            }
            stage('UT & Integration testing') {
                steps {
                    script {
                        container('build') {
                            UT()
                        }
                    }
                }
            }
            stage('SAST') {
                steps {
                    script {
                        container('sast') {
                            SAST(params)
                        }
                    }
                }
            }
            stage('dockerize') {
                steps {
                    script {
                        container('kaniko') {
                            dockerize()
                        }
                    }
                }
            }
            // stage('SecurityScan - DockerImage') {
            //     steps {
            //         script {
            //             container('trivy') {
            //                 SecurityScan()
            //             }
            //         }
            //     }
            // }
            stage('GitOps') {
                steps {
                    script {
                        container('jq') {
                            GitOps(params)
                        }
                    }
                }
            }
            stage('DAST') {
                steps {
                    script {
                        container('dast') {
                            DAST(params,env.DAST_API)
                        }
                    }
                }
            }
            stage('RaisePR') {
                when {
                    expression { params.RUN_RAISEPR }  // Only run if checkbox is ticked
                }
                steps {
                    script {
                        container('build') {
                            raisePR()
                        }
                    }
                }
            }
        }
 
    }
}
return this