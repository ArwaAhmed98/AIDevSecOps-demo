# Nginx Configuration for Ollama Proxy

When running Ollama behind nginx, you may encounter HTML error pages instead of JSON responses. This is typically due to nginx buffer size limits or timeouts.

## Recommended Nginx Configuration

Add these settings to your nginx configuration for the Ollama proxy location:

```nginx
location / {
    proxy_pass http://ollama-backend:11434;
    
    # Increase buffer sizes for large requests/responses
    proxy_buffers 16 64k;
    proxy_buffer_size 128k;
    proxy_busy_buffers_size 256k;
    
    # Increase request body size limit
    client_max_body_size 10m;
    
    # Increase timeouts for long-running LLM requests
    proxy_read_timeout 300s;
    proxy_send_timeout 300s;
    proxy_connect_timeout 60s;
    
    # Headers
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # Disable buffering for streaming responses (optional)
    proxy_buffering off;
    proxy_cache off;
    
    # WebSocket support (if needed)
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

## Key Settings Explained

- **`proxy_buffers 16 64k`**: Allocates 16 buffers of 64KB each (1MB total) for reading responses
- **`proxy_buffer_size 128k`**: Size of buffer for reading response headers
- **`client_max_body_size 10m`**: Maximum size of client request body (increase if sending large code files)
- **`proxy_read_timeout 300s`**: How long nginx waits for a response from Ollama (5 minutes)
- **`proxy_send_timeout 300s`**: How long nginx waits to send request to Ollama
- **`proxy_buffering off`**: Disables buffering for streaming responses (useful for real-time streaming)

## Common Issues

### 413 Request Entity Too Large
- **Cause**: `client_max_body_size` is too small
- **Fix**: Increase `client_max_body_size` (e.g., `10m` or `50m`)

### 502 Bad Gateway
- **Cause**: Ollama server not responding or buffer issues
- **Fix**: Check Ollama is running, increase `proxy_buffers` and `proxy_buffer_size`

### 504 Gateway Timeout
- **Cause**: `proxy_read_timeout` is too short for long LLM responses
- **Fix**: Increase `proxy_read_timeout` (e.g., `300s` or `600s`)

### HTML Error Pages Instead of JSON
- **Cause**: Nginx returning error pages due to buffer/timeout limits
- **Fix**: Apply all the settings above, especially buffer sizes and timeouts

## Testing

After updating nginx configuration:

1. Reload nginx: `sudo nginx -s reload` or `sudo systemctl reload nginx`
2. Test with a small request first
3. Monitor nginx error logs: `tail -f /var/log/nginx/error.log`

## Example Full Configuration

```nginx
upstream ollama {
    server localhost:11434;
}

server {
    listen 8080;
    server_name _;

    location / {
        proxy_pass http://ollama;
        
        proxy_buffers 16 64k;
        proxy_buffer_size 128k;
        proxy_busy_buffers_size 256k;
        client_max_body_size 10m;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
        proxy_connect_timeout 60s;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_buffering off;
        proxy_cache off;
        
        proxy_http_version 1.1;
    }
}
```

