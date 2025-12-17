def call (Map params = [:], String API){
    sh """
        cd /opt/zaproxy
        ls -la
        ./zap.sh -daemon -port 8080 -config api.key=${API} &
        sleep 160
        python3 ${env.WORKSPACE}/Dast-Scripts/Zap-scan.py --repo "${params.REPONAME}" || true
        python3 ${env.WORKSPACE}/Dast-Scripts/Zap-scan.py --repo "${params.REPONAME}"
    """
}