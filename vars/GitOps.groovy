def call(Map params = [:]){
    withCredentials([usernamePassword(credentialsId: 'GHE', 
                    usernameVariable: 'USERNAME', 
                    passwordVariable: 'PASSWORD')]) {
    
      
    def resourceBase = 'services-chart/microservices/demo-app'
    def files = [
        'values-demo.yaml'
    ]

    dir('services-chart/microservices/demo-app') {
        files.each { file ->
            writeFile(
                file: "./${file}",
                text: libraryResource("${resourceBase}/${file}")
            )
        }

        sh """
        cat values-${params.ENV}.yaml | yq eval -o=json - | \
        jq --arg tag "${env.BUILD_NUMBER}" '.image.tag = \$tag' | \
        yq eval -P - > values-${params.ENV}.yaml
        cat values-${params.ENV}.yaml
     
        git add .; git commit -m "Update the tag automated from Jenkins"; git push;
        """
        }
    }
}