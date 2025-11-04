# 测试时间线API的简单脚本

Write-Host "=== 测试时间线API ===`n"

# 测试获取所有人物API
Write-Host "测试获取所有人物API: http://localhost:8080/api/timeline/persons"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/persons" -Method Get -ContentType "application/json"
    Write-Host "成功! 人物数量: $($response.Count)`n"
    if ($response.Count -gt 0) {
        Write-Host "人物列表示例:`n" -ForegroundColor Green
        $response | Select-Object -First 3 | Format-Table -AutoSize
    }
} catch {
    Write-Host "失败: $_`n" -ForegroundColor Red
}

# 测试获取所有事件API
Write-Host "测试获取所有事件API: http://localhost:8080/api/timeline/events"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/events" -Method Get -ContentType "application/json"
    Write-Host "成功! 事件数量: $($response.Count)`n"
    if ($response.Count -gt 0) {
        Write-Host "事件列表示例:`n" -ForegroundColor Green
        $response | Select-Object -First 3 | Format-Table -AutoSize
    }
} catch {
    Write-Host "失败: $_`n" -ForegroundColor Red
}

Write-Host "=== 测试完成 ==="