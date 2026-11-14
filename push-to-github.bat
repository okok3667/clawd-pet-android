@echo off
title Push Clawd Pet to GitHub
cd /d "D:\downloads\clawd-pet-android"
echo ====================================================
echo   DANG DAY TOAN BO DU AN CLAWD PET LEN GITHUB:
echo   https://github.com/okok3667/clawd-pet-android
echo ====================================================
echo.
"D:\downloads\git\cmd\git.exe" push -u origin main -f
echo.
if %ERRORLEVEL% EQU 0 (
    echo ====================================================
    echo  [THANH CONG!] Ma nguon da duoc day len GitHub!
    echo  Ban hay vao tab Actions de doi lay file APK nhe!
    echo ====================================================
) else (
    echo [THONG BAO] Neu GitHub hoi username/token, hay dang nhap de hoan tat.
)
pause
