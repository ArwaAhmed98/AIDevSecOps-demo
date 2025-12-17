def call(){
    sh """
        cd ${env.WORKSPACE}
        ls -la
        docker build -t python-demo-app:1.0 .
    """
}