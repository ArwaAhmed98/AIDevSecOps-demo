// vars/backendEngine.groovy
def call(String AGENT = 'agents/default.yaml') {
    pipeline {
        agent {
            kubernetes {
                yaml libraryResource(AGENT)
            }
        }
        parameters {
        string(name: 'REPO_URL', defaultValue: 'https://github.com/MostafaAnas/go-cli-todo', description: 'GitHub repository URL to scan')
        string(name: 'OLLAMA_MODEL', defaultValue: 'llama3.2:1b', description: 'Ollama model to use')
        string(name: 'OLLAMA_SERVER_URL', defaultValue: 'http://172.22.24.2:8080', description: 'Ollama server URL')
        string(name: 'OUTPUT_FILE', defaultValue: '', description: 'Output file name (optional)')
        string(name: 'REPONAME', defaultValue: 'https://github.com/MostafaAnas/go-cli-todo', description: 'Repository name for DAST(e.g., org/repo or repo)')
        }
        environment {
            DAST_API = credentials('DAST-API')
        }

        stages {
            
            stage('SAST') {
                steps {
                    script {
                        container('go') {
                            SAST()
                        }
                    }
                }
            }
            stage('DAST') {
                steps {
                    script {
                        container('build') {
                            DAST(env.DAST_API)
                        }
                    }
                }
            }
        }
 
    }
}
return this