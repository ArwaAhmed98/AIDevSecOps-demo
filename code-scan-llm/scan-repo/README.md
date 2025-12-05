# Go-LLM-Code-Scan

A Go program that scans GitHub repositories for security vulnerabilities using LLM (Ollama).

## Prerequisites

1. **Go** installed (version 1.18 or later)
2. **Ollama** running locally on `http://localhost:11434`
3. **Git** installed (for cloning repositories)

## Setup

1. Initialize Go module (already done):
   ```bash
   go mod init code-scan-llm
   ```

2. Install dependencies (already done):
   ```bash
   go mod tidy
   ```

3. Make sure Ollama is running:
   ```bash
   # Start Ollama service (if not already running)
   ollama serve
   ```

4. Ensure the model is available:
   ```bash
   # Pull the model if needed (default: llama3.2:1b)
   ollama pull llama3.2:1b
   ```

## Usage

Run the program with a GitHub repository URL:

```bash
go run main.go <github-repo-url>
```

### Example

```bash
go run main.go https://github.com/user/repo
```

### Using Environment Variables

You can configure the Ollama model and server URL using environment variables:

```bash
# Set environment variables and run
export OLLAMA_MODEL="llama3.1:8b"
export OLLAMA_SERVER_URL="http://localhost:11434"
go run main.go https://github.com/user/repo
```

Or inline:

```bash
OLLAMA_MODEL="llama3.1:8b" OLLAMA_SERVER_URL="http://localhost:11434" go run main.go https://github.com/user/repo
```

## How it works

1. Clones the specified GitHub repository to a temporary directory
2. Scans all supported code files (.go, .py, .sql, .js, .java, .cpp, .c, .rb, .php, .ts, .sh)
3. Sends each file to the LLM for security analysis
4. Displays vulnerability analysis results

## Configuration

### Environment Variables

- **`OLLAMA_MODEL`**: The Ollama model to use (default: `llama3.2:1b`)
  ```bash
  export OLLAMA_MODEL="llama3.1:8b"
  ```

- **`OLLAMA_SERVER_URL`**: The Ollama server URL (default: `http://localhost:11434`)
  ```bash
  export OLLAMA_SERVER_URL="http://localhost:11434"
  ```

### Other Configuration

- **System Message**: Edit `systemmessage.txt` to customize the security analysis prompt