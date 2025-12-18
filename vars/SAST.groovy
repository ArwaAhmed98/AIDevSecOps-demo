def call(Map params = [:]) {


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

dir('code-scan-llm/scan-repo') {
    files.each { file ->
        writeFile(
            file: "./${file}",
            text: libraryResource("${resourceBase}/${file}")
        )
    }

    // -----------------------------
    // Run scan
    // -----------------------------
    
        sh """
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
            ls -lRa
            pwd
        """
    }

    // -----------------------------
    // Archive results
    // -----------------------------
    archiveArtifacts artifacts: './scan-results-*.*',
                     allowEmptyArchive: true
}
