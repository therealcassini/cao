$body = Get-Content -Raw test-search-config.json
try {
    $response = Invoke-WebRequest -Uri http://localhost:8080/api/search-configs -Method POST -ContentType 'application/json' -Body $body -UseBasicParsing
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "Error: $_"
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $statusDescription = $_.Exception.Response.StatusDescription
        Write-Host "HTTP Status Code: $statusCode - $statusDescription"
        
        # 尝试读取错误响应内容
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorContent = $reader.ReadToEnd()
            Write-Host "Error Content: $errorContent"
        } catch {}
    }
}