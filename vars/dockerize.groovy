def call(){
    IMAGE_NAME="ar5678/aidevsecops:${env.BUILD_NUMBER}"
    sh """
    ls -la /kaniko/.docker/
    # Kaniko command
    /kaniko/executor \
        --dockerfile Dockerfile \
        --context . \
        --destination=$IMAGE_NAME \
        --verbosity info
    """
}
