def call(Map params = [:]) {

    // Provide defaults (optional but recommended)
    params.OLLAMA_MODEL       = params.get('OLLAMA_MODEL', 'llama3')
    params.OLLAMA_SERVER_URL  = params.get('OLLAMA_SERVER_URL', 'http://ollama:11434')
    params.OUTPUT_FILE        = params.get('OUTPUT_FILE', '')
    params.REPO_URL           = params.get('REPO_URL', '')

    sh """
        set -e
        ls -R
        cd ${env.WORKSPACE}/code-scan-llm/scan-repo
        # Initialize Go module if needed
        if [ ! -f go.mod ]; then
            go mod init code-scan-llm
        fi

        # Install dependencies
        go mod tidy

        # Export environment variables
        export OLLAMA_MODEL="${params.OLLAMA_MODEL}"
        export OLLAMA_SERVER_URL="${params.OLLAMA_SERVER_URL}"

        if [ -n "${params.OUTPUT_FILE}" ]; then
            export OUTPUT_FILE="${params.OUTPUT_FILE}"
        fi

        # Run SAST scan
        go run main.go "${params.REPO_URL}"
    """

    // Archive results
    archiveArtifacts artifacts: 'scan-results-*.txt', allowEmptyArchive: true
}
    

