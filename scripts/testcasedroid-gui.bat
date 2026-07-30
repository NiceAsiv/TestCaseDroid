@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
java %JAVA_OPTS% -jar "%SCRIPT_DIR%..\lib\TestCaseDroid.jar" %*
exit /b %ERRORLEVEL%
