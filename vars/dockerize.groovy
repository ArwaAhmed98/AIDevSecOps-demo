def call(){
    sh """
        cd ${env.WORKSPACE}
        docker build -t python-demo-app:1.0 .
    """
}