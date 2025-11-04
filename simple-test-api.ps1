# 简单的API测试脚本
Write-Host "测试后端API..."

# 测试获取人物API
try {
    Write-Host "`n测试人物API:"
    Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/persons" -Method Get -ContentType "application/json"
    Write-Host "成功!"
} catch {
    Write-Host "失败: $_"
}

# 测试获取事件API
try {
    Write-Host "`n测试事件API:"
    Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/events" -Method Get -ContentType "application/json"
    Write-Host "成功!"
} catch {
    Write-Host "失败: $_"
}