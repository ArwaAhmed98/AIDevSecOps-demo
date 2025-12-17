def call(Map params = [:]) {

    // -----------------------------
    // Defaults
    // -----------------------------
    params.OLLAMA_MODEL      = params.get('OLLAMA_MODEL', 'llama3')
    params.OLLAMA_SERVER_URL = params.get('OLLAMA_SERVER_URL', 'http://ollama:11434')
    params.OUTPUT_FILE       = params.get('OUTPUT_FILE', '')
    params.REPO_URL          = params.get('REPO_URL', '')

    if (!params.REPO_URL?.trim()) {
        error "REPO_URL is required"
    }

    // -----------------------------
    // Extract shared-lib resources
    // -----------------------------
    def resourceBase = 'code-scan-llm/scan-repo'
    def files = [
        'go.mod',
        'go.sum',
        'main.go',
        'query.py',
        'systemmessage.txt',
        'README.md',
        'nginx-config.md'
    ]

    sh 'rm -rf code-scan-llm && mkdir -p code-scan-llm/scan-repo'

    files.each { file ->
        writeFile(
            file: "code-scan-llm/scan-repo/${file}",
            text: libraryResource("${resourceBase}/${file}")
        )
    }

    // -----------------------------
    // Run scan
    // -----------------------------
    dir('code-scan-llm/scan-repo') {
        sh """
            set -e
            echo "Workspace: \$(pwd)"
            ls -la

            # Initialize Go module if missing
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
    }

    // -----------------------------
    // Archive results
    // -----------------------------
    archiveArtifacts artifacts: 'code-scan-llm/scan-repo/scan-results-*.*',
                     allowEmptyArchive: true
}
