def call (){
    sh """
    pip install --no-cache-dir -r requirements.txt
    flask run --host=0.0.0.0 --port=5000 &
    """
}