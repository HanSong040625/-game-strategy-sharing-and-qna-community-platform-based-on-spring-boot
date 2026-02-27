@echo off
echo 私聊消息表创建脚本
echo ================================
echo.
echo 请确保MySQL服务正在运行，并且已连接到game_community数据库
echo.
echo 请手动执行以下SQL语句：
echo.
type private_message_table.sql
echo.
echo ================================
echo 执行步骤：
echo 1. 打开MySQL客户端（如phpMyAdmin、MySQL Workbench或命令行）
echo 2. 连接到game_community数据库
echo 3. 复制上面的SQL语句并执行
echo 4. 执行完成后重启后端服务
echo.
pause