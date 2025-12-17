def call(){
    sh """
    trivy image --severity HIGH,CRITICAL python-demo-app:${env.BUILD_NUMBER} || true
    """
}