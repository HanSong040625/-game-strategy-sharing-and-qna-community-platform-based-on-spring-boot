# 私聊消息表创建脚本
# 这个脚本将执行私聊消息表的创建SQL

Write-Host "开始创建私聊消息表..." -ForegroundColor Green

# 检查MySQL是否可用
try {
    $mysqlPath = Get-Command mysql -ErrorAction Stop
    Write-Host "MySQL客户端找到: $($mysqlPath.Source)" -ForegroundColor Green
} catch {
    Write-Host "错误: MySQL客户端未找到，请确保MySQL已安装并添加到PATH环境变量" -ForegroundColor Red
    Write-Host "或者请手动执行以下SQL语句:" -ForegroundColor Yellow
    Get-Content "private_message_table.sql" | Write-Host
    exit 1
}

# 读取SQL文件内容
$sqlContent = Get-Content -Path "private_message_table.sql" -Raw

# 执行SQL语句
Write-Host "正在执行私聊消息表创建SQL..." -ForegroundColor Yellow

try {
    # 使用MySQL命令行工具执行SQL
    $result = & mysql -u root -p -e "USE game_community; $sqlContent" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "私聊消息表创建成功!" -ForegroundColor Green
        Write-Host "请重启后端服务以应用更改" -ForegroundColor Cyan
    } else {
        Write-Host "创建表时出现错误:" -ForegroundColor Red
        Write-Host $result -ForegroundColor Red
        Write-Host ""
        Write-Host "请手动执行以下SQL语句:" -ForegroundColor Yellow
        Write-Host $sqlContent
    }
} catch {
    Write-Host "执行SQL时出现异常: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "请手动执行以下SQL语句:" -ForegroundColor Yellow
    Write-Host $sqlContent
}

Write-Host ""
Write-Host "脚本执行完成" -ForegroundColor Green