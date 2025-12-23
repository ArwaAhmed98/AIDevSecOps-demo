def call(Map params = [:]){
    withCredentials([usernamePassword(credentialsId: 'GHE_VODAFONE', 
                    usernameVariable: 'USERNAME', 
                    passwordVariable: 'PASSWORD')]) {
        
        sh """
        
        git clone https://${PASSWORD}@github.vodafone.com/VFCOM-CICD/AIDevSecOps-demo.git
        git config --global user.name "arwa-abdlhalim"
        cd AIDevSecOps-demo/resources/services-chart/microservices/${env.repoName}
        cat values-${params.ENV}.yaml | yq eval -o=json - | \
        jq --arg tag "${env.BUILD_NUMBER}" '.image.tag = \$tag' | \
        yq eval -P - > values-${params.ENV}.yaml
        cat values-${params.ENV}.yaml
        git config --global user.name "arwa-abdlhalim"
        git config --global user.email "arwa.abdelhalim@vodafone.com"
        git add .; git commit -m "Update the tag automated from Jenkins"; git push;
        """
    }
}
    
