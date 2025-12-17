def call (Map params = [:], String API){
     def resourceBase = 'Dast-Scripts'
     def files = [
        'Createpr.py',
        'Zap-scan.py',
        'extract-code.py',
        'groovy-script',
        'python.py'
    ]

    dir('Dast-Scripts') {
        files.each { file ->
            writeFile(
                file: "./${file}",
                text: libraryResource("${resourceBase}/${file}")
            )
        }

        sh """
            
            cd /opt/zaproxy
            ls -la
            ./zap.sh -daemon -port 8080 -config api.key=${API} &
            sleep 160
            cd -
            python3 ./Zap-scan.py --repo "${params.REPONAME}" || true
            python3 ./Zap-scan.py --repo "${params.REPONAME}"
        """
    }   

}




   