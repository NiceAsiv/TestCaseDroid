@echo off
setlocal

REM Check if the arguments are empty
IF "%~1"=="" (
    echo Please enter the project path and the test class name
    echo Example: C:\Users\user\Project\src\test\java com.example.TestClass
    exit /b
)
IF "%~2"=="" (
    echo Please enter the project path and the test class name
    echo Example: C:\Users\user\Project\src\test\java com.example.TestClass
    exit /b
)

set "PROJECT_PATH=%~1"
set "TEST_CLASS_NAME=%~2"

echo Project path: %PROJECT_PATH%
echo Test class name: %TEST_CLASS_NAME%

REM Check if the project path is valid
IF NOT EXIST "%PROJECT_PATH%" (
    echo Invalid project path
    exit /b
)

REM Run the test case using Maven
FOR /F "tokens=*" %%A IN ('mvn -f "%PROJECT_PATH%" test -Dtest="%TEST_CLASS_NAME%"') DO (
    set "OUTPUT=%%A"
)

REM Check if the output is empty
IF "%OUTPUT%"=="" (
    echo Test case not found
    exit /b
)

REM initialize the variables
set "TOTAL_TESTS=0"
set "FAILED_TESTS=0"
set "ERROR_TESTS=0"
set "SKIPPED_TESTS=0"

REM Parse the test results line by line
FOR /F "tokens=1,2,3,4 delims=," %%A IN ("%OUTPUT%") DO (
    IF "%%A"=="Tests run" (
        set "TOTAL_TESTS=%%~nxB"
    )
    IF "%%B"=="Failures" (
        set "FAILED_TESTS=%%~nxB"
    )
    IF "%%C"=="Errors" (
        set "ERROR_TESTS=%%~nxB"
    )
    IF "%%D"=="Skipped" (
        set "SKIPPED_TESTS=%%~nxB"
    )
)

REM Print the test results
echo Total tests: %TOTAL_TESTS%
echo Failed tests: %FAILED_TESTS%
echo Error tests: %ERROR_TESTS%
echo Skipped tests: %SKIPPED_TESTS%

endlocal