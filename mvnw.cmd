@echo off
setlocal
set "MAVEN_VERSION=3.9.11"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%"
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto run_maven

echo Maven %MAVEN_VERSION% is not cached. Downloading the official Apache Maven distribution...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $v='%MAVEN_VERSION%'; $root=Join-Path $env:USERPROFILE '.m2\wrapper\dists'; New-Item -ItemType Directory -Force -Path $root | Out-Null; $zip=Join-Path $root ('apache-maven-'+$v+'-bin.zip'); $url='https://archive.apache.org/dist/maven/maven-3/'+$v+'/binaries/apache-maven-'+$v+'-bin.zip'; Invoke-WebRequest -Uri $url -OutFile $zip; Expand-Archive -LiteralPath $zip -DestinationPath $root -Force; Remove-Item -LiteralPath $zip -Force"
if errorlevel 1 (
  echo Failed to download or unpack Maven. Check your internet connection and try again.
  exit /b 1
)

:run_maven
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
