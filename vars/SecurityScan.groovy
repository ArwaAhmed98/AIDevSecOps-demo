def call(){
    sh """
    trivy image --timeout 15m --severity HIGH,CRITICAL python-demo-app:${env.BUILD_NUMBER} || true
    """
}