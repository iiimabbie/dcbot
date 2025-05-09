@echo off
chcp 65001

:menu
cls
echo ====================================
echo Docker 容器重啟腳本
echo ====================================
echo 0 - 單純重啟容器
echo 1 - 完全重建和重新編譯
echo X - exit
echo ====================================
set /p choice=請選擇操作 [0/1/X]: 

if "%choice%"=="0" (
    echo 正在重啟容器...
    docker compose restart
    echo 容器已重啟完成！
) else if "%choice%"=="1" (
    echo 正在重建和重新編譯...
    docker compose down
    docker compose up -d
    echo 重建和重新編譯完成！
) else if /i "%choice%"=="X" (
    echo 正在退出程式...
    exit /b 0
) else (
    color 0C
    echo 錯誤：無效的選擇！
    timeout /t 2 >nul
    color 0B
    goto menu
)

echo ====================================
echo 容器狀態：
docker compose ps
echo ====================================
pause
goto menu