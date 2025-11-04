# 使用curl测试后端API的PowerShell脚本

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
    
    # 使用curl命令发送GET请求
    curl -X GET $fullUrl -v
    
    Write-Host "----------------------------"
}

Write-Host "API测试完成!"