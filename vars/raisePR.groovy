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
        sh """
            python3 ./extract-code.py
            python3 ./Createpr.py
        """
    }   

}




   