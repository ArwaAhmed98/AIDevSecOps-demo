def call(Map params = [:]) {

    // -----------------------------
    // Handle parameters & defaults
    // -----------------------------
    def repoUrl        = params.REPO_URL ?: this.params.REPO_URL ?: 'https://github.com/MostafaAnas/go-cli-todo'
    def ollamaModel    = params.OLLAMA_MODEL ?: 'llama3'
    def ollamaServer   = params.OLLAMA_SERVER_URL ?: 'http://ollama:11434'
    def outputFile     = params.OUTPUT_FILE ?: ''

    if (!repoUrl?.trim()) {
        error "REPO_URL is required"
    }

    // -----------------------------
    // Temporary workspace per build
    // -----------------------------
    def tmpDir = "${env.WORKSPACE}/tmp-scan-${BUILD_NUMBER}"
    sh "mkdir -p ${tmpDir}"

    // -----------------------------
    // Shared library resource base
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

    // -----------------------------
    // Extract resources to tmpDir
    // -----------------------------
    files.each { file ->
        writeFile file: "${tmpDir}/${file}", text: libraryResource("${resourceBase}/${file}")
    }

    // -----------------------------
    // Run Go SAST scan
    // -----------------------------
    dir(tmpDir) {
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
            export OLLAMA_MODEL="${ollamaModel}"
            export OLLAMA_SERVER_URL="${ollamaServer}"

            if [ -n "${outputFile}" ]; then
                export OUTPUT_FILE="${outputFile}"
            fi

            # Run SAST scan
            go run main.go "${repoUrl}"
        """
    }

    // -----------------------------
    // Archive results
    // -----------------------------
    archiveArtifacts artifacts: "${tmpDir}/scan-results-*.*", allowEmptyArchive: true
}
