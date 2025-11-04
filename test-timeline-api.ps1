# 测试历史时间线API的PowerShell脚本
Write-Host "测试历史时间线API..."

# 测试获取所有人物API
Write-Host "`n1. 测试获取所有人物:"
try {
    $personsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/persons" -Method Get -ContentType "application/json"
    Write-Host "成功! 返回了 $($personsResponse.Count) 个人物数据"
    if ($personsResponse.Count -gt 0) {
        Write-Host "前3个人物:"
        $personsResponse | Select-Object -First 3 | Format-Table -AutoSize
    }
} catch {
    Write-Host "错误: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) {
        Write-Host "详细错误: $($_.ErrorDetails.Message)"
    }
}

# 测试获取所有事件API
Write-Host "`n2. 测试获取所有事件:"
try {
    $eventsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/events" -Method Get -ContentType "application/json"
    Write-Host "成功! 返回了 $($eventsResponse.Count) 个事件数据"
    if ($eventsResponse.Count -gt 0) {
        Write-Host "前3个事件:"
        $eventsResponse | Select-Object -First 3 | Format-Table -AutoSize
    }
} catch {
    Write-Host "错误: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) {
        Write-Host "详细错误: $($_.ErrorDetails.Message)"
    }
}

# 测试按年份获取事件API
Write-Host "`n3. 测试获取1949年事件:"
try {
    $yearResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/timeline/events/year/1949" -Method Get -ContentType "application/json"
    Write-Host "成功! 返回了 $($yearResponse.Count) 个事件数据"
    if ($yearResponse.Count -gt 0) {
        $yearResponse | Format-Table -AutoSize
    }
} catch {
    Write-Host "错误: $($_.Exception.Message)"
    if ($_.ErrorDetails.Message) {
        Write-Host "详细错误: $($_.ErrorDetails.Message)"
    }
}