# 测试后端API是否正常工作的PowerShell脚本

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
        # 发送GET请求并获取响应
        $response = Invoke-RestMethod -Uri $fullUrl -Method Get -TimeoutSec 5
        
        # 显示响应状态
        Write-Host "✅ 成功 - 状态码: 200"
        
        # 如果响应包含数据，显示数据类型和长度
        if ($response -ne $null) {
            if ($response -is [array]) {
                Write-Host "   响应包含: 数组，长度 $($response.Length)"
                # 显示第一个元素(如果有)
                if ($response.Length -gt 0) {
                    Write-Host "   示例数据: " (ConvertTo-Json -InputObject $response[0] -Depth 2 -Compress)
                }
            } else {
                Write-Host "   响应包含: 对象"
                Write-Host "   键: " ($response.PSObject.Properties.Name -join ", ")
            }
        } else {
            Write-Host "   响应为空"
        }
    } catch {
        # 显示错误信息
        Write-Host "❌ 失败"
        Write-Host "   错误: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode.value__
            $statusDescription = $_.Exception.Response.StatusDescription
            Write-Host "   状态码: $statusCode - $statusDescription"
        }
    }
    
    Write-Host "----------------------------"
}

Write-Host "API测试完成!"