def call(Map params = [:]){
    withCredentials([usernamePassword(credentialsId: 'GHE', 
                    usernameVariable: 'USERNAME', 
                    passwordVariable: 'PASSWORD')]) {
        
        sh """
        git clone https://${PASSWORD}@github.vodafone.com/VFCOM-CICD/AIDevSecOps-demo.git
        cd resources/services-chart/microservices/${env.repoName}
        cat values-${params.ENV}.yaml | yq eval -o=json - | \
        jq --arg tag "${env.BUILD_NUMBER}" '.image.tag = \$tag' | \
        yq eval -P - > values-${params.ENV}.yaml
        cat values-${params.ENV}.yaml
     
        git add .; git commit -m "Update the tag automated from Jenkins"; git push;
        """
    }
}
    
