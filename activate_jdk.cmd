@echo off
setlocal EnableDelayedExpansion
set "BASE=%~dp0"
if "%BASE:~-1%"=="\" set "BASE=%BASE:~0,-1%"

set "SELECTED="
if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot\bin\java.exe" (
  set "SELECTED=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
) else (
  for %%D in (
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Microsoft\jdk-17*"
    "C:\Program Files\Microsoft\jdk-21*"
    "C:\Program Files\Java\jdk-17*"
    "C:\Program Files\Java\jdk-21*"
  ) do (
    if exist "%%~D\bin\java.exe" (
      set "SELECTED=%%~fD"
      goto found
    )
  )
)

:found
if not defined SELECTED (
  echo [ERROR] No JDK 17/21 found.
  echo SELECTED=NONE > "%BASE%\jdk_status.txt"
  exit /b 1
)

echo %SELECTED% > "%BASE%\jdk_path.txt"
set "JAVA_HOME=%SELECTED%"
set "PATH=%JAVA_HOME%\bin;%PATH%"
"%JAVA_HOME%\bin\java.exe" -version > "%BASE%\java_version.txt" 2>&1
set "STATUS=%ERRORLEVEL%"
if "%STATUS%" NEQ "0" (
  echo [ERROR] Java version command failed.
  echo STATUS=%STATUS% > "%BASE%\jdk_status.txt"
  exit /b %STATUS%
)

echo OK > "%BASE%\jdk_status.txt"
cd /d "%BASE%\PlanningService\planning-service"
call mvnw -q -DskipTests compile > "%BASE%\planning_compile.txt" 2>&1
set "RESULT=%ERRORLEVEL%"
if "%RESULT%" NEQ "0" (
  echo COMPILE_FAILED > "%BASE%\planning_compile_status.txt"
  exit /b %RESULT%
)

echo COMPILE_OK > "%BASE%\planning_compile_status.txt"
echo [OK] Using Java from %JAVA_HOME%
