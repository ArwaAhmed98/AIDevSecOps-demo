def call(){
        IMAGE_NAME= "${env.repoName}:${env.BUILD_NUMBER}"
        sh """
        ls -la /kaniko/.docker/
        /kaniko/executor \
            --dockerfile Dockerfile \
            --context . \
            --destination=$IMAGE_NAME \
            --verbosity info
        """
}
