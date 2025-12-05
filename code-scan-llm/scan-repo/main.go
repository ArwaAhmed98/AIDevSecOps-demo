package main

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/czc09/langchaingo/llms"
	"github.com/czc09/langchaingo/llms/ollama"
)

// readFile reads the content of a file given its path.
func readFile(filePath string) (string, error) {
	codeBytes, err := os.ReadFile(filePath)
	if err != nil {
		return "", fmt.Errorf("error reading file %s: %v", filePath, err)
	}
	return string(codeBytes), nil
}

// cloneRepo clones a GitHub repository to a local directory.
func cloneRepo(repoURL, localPath string) error {
	cmd := exec.Command("git", "clone", repoURL, localPath)
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	return cmd.Run()
}

// checkOllamaServer checks if the Ollama server is accessible and returns JSON
func checkOllamaServer(serverURL, model string) error {
	baseURL := strings.TrimSuffix(serverURL, "/")
	client := &http.Client{
		Timeout: 10 * time.Second,
	}

	// First, check if the server responds with JSON (test /api/tags endpoint)
	healthURL := baseURL + "/api/tags"
	resp, err := client.Get(healthURL)
	if err != nil {
		return fmt.Errorf("failed to connect to Ollama server at %s: %v. please ensure the server is running and accessible", serverURL, err)
	}
	defer resp.Body.Close()

	// Read response body to check if it's JSON
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("failed to read response from Ollama server: %v", err)
	}

	// Check if response is HTML (error page)
	if len(body) > 0 && body[0] == '<' {
		return fmt.Errorf("ollama server at %s returned HTML instead of JSON. this usually means the server URL is incorrect or the server is not an Ollama instance. response preview: %s", serverURL, string(body[:min(200, len(body))]))
	}

	// Try to parse as JSON to verify it's valid
	var jsonData interface{}
	if err := json.Unmarshal(body, &jsonData); err != nil {
		return fmt.Errorf("ollama server at %s returned invalid JSON response. response preview: %s", serverURL, string(body[:min(200, len(body))]))
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("ollama server returned non-OK status: %d. server may not be properly configured", resp.StatusCode)
	}

	// Verify the model exists by checking if it's in the tags list
	if model != "" {
		var tagsResp struct {
			Models []struct {
				Name string `json:"name"`
			} `json:"models"`
		}
		if err := json.Unmarshal(body, &tagsResp); err == nil {
			modelFound := false
			for _, m := range tagsResp.Models {
				if m.Name == model || strings.HasPrefix(m.Name, model+":") {
					modelFound = true
					break
				}
			}
			if !modelFound {
				log.Printf("warning: model '%s' not found in available models. available models: %v", model, tagsResp.Models)
			}
		}
	}

	return nil
}

func min(a, b int) int {
	if a < b {
		return a
	}
	return b
}

// analyzeCode sends the code and system message to the LLM for analysis.
func analyzeCode(ctx context.Context, llm llms.Model, systemMessage, code string, outputWriter io.Writer) error {
	content := []llms.MessageContent{
		llms.TextParts(llms.ChatMessageTypeSystem, systemMessage),
		llms.TextParts(llms.ChatMessageTypeHuman, code),
	}

	// Streaming function to handle LLM output
	streamingFunc := func(ctx context.Context, chunk []byte) error {
		// Write to output writer (which includes both stdout and file)
		if outputWriter != nil {
			_, err := outputWriter.Write(chunk)
			if err != nil {
				return err
			}
		}
		return nil
	}

	// Generate content with streaming
	_, err := llm.GenerateContent(ctx, content, llms.WithStreamingFunc(streamingFunc))
	if err != nil {
		// Provide more context for JSON parsing errors
		if strings.Contains(err.Error(), "invalid character '<'") {
			return fmt.Errorf("received HTML response instead of JSON from Ollama server during API call. this usually means: 1) the /api/generate or /api/chat endpoint is not working, 2) server returned an error page, 3) there's a proxy/load balancer issue. original error: %v", err)
		}
		return fmt.Errorf("llm API error: %v", err)
	}
	return nil
}

// scanRepo scans a GitHub repository for security vulnerabilities.
func scanRepo(ctx context.Context, llm llms.Model, repoURL, systemMessage string, outputWriter io.Writer) error {
	// Create a temporary directory to clone the repository
	tempDir, err := os.MkdirTemp("", "repo-*")
	if err != nil {
		return fmt.Errorf("failed to create temp directory: %v", err)
	}
	defer os.RemoveAll(tempDir) // Clean up the temp directory

	// Clone the repository
	if err := cloneRepo(repoURL, tempDir); err != nil {
		return fmt.Errorf("failed to clone repository: %v", err)
	}

	// Walk through the repository and analyze each file
	err = filepath.Walk(tempDir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}

		// Supported file extensions
		var supportedExtensions = map[string]bool{
			".go":   true, // Go
			".py":   true, // Python
			".sql":  true, // SQL
			".js":   true, // JavaScript
			".java": true, // Java
			".cpp":  true, // C++
			".c":    true, // C
			".rb":   true, // Ruby
			".php":  true, // PHP
			".ts":   true, // TypeScript
			".sh":   true, // Shell Script
			// Add more extensions as needed
		}

		// Skip directories and non-Go files
		if info.IsDir() || !supportedExtensions[filepath.Ext(info.Name())] {
			return nil
		}

		// Read the file content
		code, err := readFile(path)
		if err != nil {
			return fmt.Errorf("failed to read file %s: %v", path, err)
		}

		// Analyze the file
		fileInfo := fmt.Sprintf("Analyzing file: %s\n", path)
		if outputWriter != nil {
			outputWriter.Write([]byte(fileInfo))
		}

		if err := analyzeCode(ctx, llm, systemMessage, code, outputWriter); err != nil {
			return fmt.Errorf("failed to analyze file %s: %v", path, err)
		}

		// Add separator between files
		separator := "\n" + strings.Repeat("=", 80) + "\n\n"
		if outputWriter != nil {
			outputWriter.Write([]byte(separator))
		}

		return nil
	})

	return err
}

func main() {
	// Check command-line arguments
	if len(os.Args) < 2 {
		log.Fatal("Usage: go run main.go")
	}

	// Get the GitHub repository URL
	repoURL := os.Args[1]

	// Get the current working directory
	basePath, err := os.Getwd()
	if err != nil {
		log.Fatalf("Error getting current directory: %v\n", err)
	}

	// Read the system message from file
	systemFilePath := filepath.Join(basePath, "systemmessage.txt")
	systemMessage, err := readFile(systemFilePath)
	if err != nil {
		log.Fatalf("Failed to read system message: %v\n", err)
	}

	// Get model and server URL from environment variables with defaults
	model := os.Getenv("OLLAMA_MODEL")
	if model == "" {
		model = "llama3.2:1b" // Default model
	}

	serverURL := os.Getenv("OLLAMA_SERVER_URL")
	if serverURL == "" {
		serverURL = "http://localhost:11434" // Default server URL
	}

	// Check if Ollama server is accessible before initializing
	fmt.Printf("Checking Ollama server connection at %s...\n", serverURL)
	if err := checkOllamaServer(serverURL, model); err != nil {
		log.Fatalf("Ollama server check failed: %v\n", err)
	}
	fmt.Printf("Ollama server is accessible and responding with JSON.\n")

	// Initialize the Ollama LLM
	fmt.Printf("Initializing LLM with model: %s\n", model)
	llm, err := ollama.New(
		ollama.WithModel(model),
		ollama.WithServerURL(serverURL),
	)
	if err != nil {
		log.Fatalf("Failed to initialize LLM: %v\n", err)
	}
	fmt.Printf("LLM initialized successfully.\n")

	// Test the API with a small request to verify the generate endpoint works
	fmt.Printf("Testing API connection with a small request...\n")
	testCtx, testCancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer testCancel()
	testContent := []llms.MessageContent{
		llms.TextParts(llms.ChatMessageTypeHuman, "Say 'OK'"),
	}
	_, testErr := llm.GenerateContent(testCtx, testContent)
	if testErr != nil {
		if strings.Contains(testErr.Error(), "invalid character '<'") {
			log.Fatalf("API test failed: received HTML response from /api/generate endpoint. the server URL %s may be incorrect or the endpoint is not accessible. error: %v\n", serverURL, testErr)
		}
		log.Fatalf("API test failed: %v. please verify the server URL and model are correct\n", testErr)
	}
	fmt.Printf("API test successful. proceeding with scan...\n\n")

	// Get output file path from environment variable or use default
	outputFile := os.Getenv("OUTPUT_FILE")
	if outputFile == "" {
		// Generate default filename based on timestamp
		timestamp := time.Now().Format("20060102-150405")
		outputFile = fmt.Sprintf("scan-results-%s.md", timestamp)
	}

	// Create/open output file
	file, err := os.Create(outputFile)
	if err != nil {
		log.Fatalf("Failed to create output file: %v\n", err)
	}
	defer file.Close()

	// Create a multi-writer to write to both stdout and file
	outputWriter := io.MultiWriter(os.Stdout, file)

	// Write header to both stdout and file
	header := fmt.Sprintf("Security Scan Results\nRepository: %s\nScan Date: %s\n%s\n\n",
		repoURL,
		time.Now().Format("2006-01-02 15:04:05"),
		strings.Repeat("=", 80))
	outputWriter.Write([]byte(header))
	fmt.Printf("Writing output to: %s\n\n", outputFile)

	// Create a context with timeout
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Minute) // Adjust timeout as needed
	defer cancel()

	// Scan the GitHub repository
	if err := scanRepo(ctx, llm, repoURL, systemMessage, outputWriter); err != nil {
		log.Fatalf("Error scanning repository: %v\n", err)
	}

	fmt.Printf("\n\nScan completed! Results saved to: %s\n", outputFile)
}
