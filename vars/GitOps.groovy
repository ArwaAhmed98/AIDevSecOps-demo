def call(){
    withCredentials([usernamePassword(credentialsId: 'GHE', 
                    usernameVariable: 'USERNAME', 
                    passwordVariable: 'PASSWORD')]) {
    
      
    def resourceBase = 'code-scan-llm/scan-repo'
    def files = [
        'go.mod',
        'go.sum',
        'main.go',
        'query.py',
        'systemmessage.txt',
        'README.md',
        'nginx-config.md'
    ]

    dir('code-scan-llm/scan-repo') {
        files.each { file ->
            writeFile(
                file: "./${file}",
                text: libraryResource("${resourceBase}/${file}")
            )
        }

                sh """
                """
            }
    }
}