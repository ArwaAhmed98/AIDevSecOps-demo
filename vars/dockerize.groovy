def call(){

    IMAGE_NAME="ar5678/aidevsecops:${BUILD_NUMBER}"
    sh """"
    # Kaniko command
    /kaniko/executor \
        --dockerfile Dockerfile \
        --context . \
        --destination=$IMAGE_NAME \
        --docker-config /kaniko/.docker/ \
        --verbosity info
    """
}