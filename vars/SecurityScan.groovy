def call(){
    sh """
    trivy image --severity HIGH,CRITICAL python-demo-app:1.0 || true
    """
}