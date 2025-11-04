# 使用PowerShell原生命令测试后端API

# 设置输出编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 后端服务器地址
$backendUrl = "http://localhost:8080"

# 定义要测试的API端点
$endpoints = @(
    "/api/test/connection",
    "/api/test/sample-graph",
    "/api/themes",
    "/api/nodes",
    "/api/edges"
)

Write-Host "开始测试后端API..."
Write-Host "后端服务器地址: $backendUrl"
Write-Host "----------------------------"

# 循环测试每个端点
foreach ($endpoint in $endpoints) {
    $fullUrl = "$backendUrl$endpoint"
    Write-Host "测试端点: $endpoint"
    
    try {
        # 使用Invoke-WebRequest发送GET请求
        $response = Invoke-WebRequest -Uri $fullUrl -Method Get -TimeoutSec 5 -ErrorAction Stop
        
        Write-Host "状态码: $($response.StatusCode)"
        Write-Host "响应长度: $($response.Content.Length) 字节"
        
        # 尝试解析JSON响应
        try {
            $content = ConvertFrom-Json -InputObject $response.Content
            Write-Host "响应类型: JSON"
            if ($content -is [array]) {
                Write-Host "数组长度: $($content.Length)"
                if ($content.Length -gt 0) {
                    Write-Host "第一个元素示例: " (ConvertTo-Json -InputObject $content[0] -Depth 2 -Compress)
                }
            }
        } catch {
            Write-Host "响应不是有效的JSON格式"
        }
    } catch {
        Write-Host "请求失败: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            Write-Host "状态码: $statusCode"
        }
    }
    
    Write-Host "----------------------------"
}

Write-Host "API测试完成!"