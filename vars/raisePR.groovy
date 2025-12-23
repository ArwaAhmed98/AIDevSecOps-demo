def call (){
     def resourceBase = 'Dast-Scripts'
     def files = [
        'extract-code.py',
        'Createpr.py'
    ]

    dir('Dast-Scripts') {
        files.each { file ->
            writeFile(
                file: "./${file}",
                text: libraryResource("${resourceBase}/${file}")
            )
        }
        withCredentials([usernamePassword(
                                credentialsId: 'GHE',
                                usernameVariable: 'USER',
                                passwordVariable: 'PASS'
                            )]) {
                sh """
                    export GITHUB_TOKEN=${PASS}
                    python3 ./extract-code.py
                    python3 ./Createpr.py
                """
        }
    }   

}




   