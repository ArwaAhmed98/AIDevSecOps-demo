def call (){
     def resourceBase = 'Dast-Scripts'
     def files = [
        'extract-code.py',
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
        """
    }   

}




   