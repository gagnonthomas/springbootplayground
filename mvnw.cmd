@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"
set "MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

for /f "tokens=1,* delims==" %%a in ('findstr "distributionUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set "DIST_URL=%%b"
for /f "tokens=1,* delims==" %%a in ('findstr "wrapperUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set "WRAPPER_URL=%%b"

for %%i in ("%DIST_URL%") do set "DIST_NAME=%%~ni"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%"

if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%MAVEN_WRAPPER_JAR%'"
)

if not exist "%MAVEN_HOME%" (
    mkdir "%MAVEN_HOME%"
    echo Downloading Maven...
    powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%TEMP%\%DIST_NAME%.zip'"
    powershell -Command "Expand-Archive -Path '%TEMP%\%DIST_NAME%.zip' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%TEMP%\%DIST_NAME%.zip"
)

for /r "%MAVEN_HOME%" %%i in (mvn.cmd) do set "MVN_CMD=%%i"
"%MVN_CMD%" %*
