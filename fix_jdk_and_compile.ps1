$ErrorActionPreference = 'Stop'
$jdk = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$javaExe = Join-Path $jdk 'bin\java.exe'
$javacExe = Join-Path $jdk 'bin\javac.exe'
$compileLog = 'c:\planora\compile_log.txt'
$javaLog = 'c:\planora\java_log.txt'

if (-not (Test-Path $javaExe)) {
  throw "JDK not found: $jdk"
}

$env:JAVA_HOME = $jdk
$env:PATH = (Join-Path $jdk 'bin') + ';' + $env:PATH

& $javaExe -version *> $javaLog
& $javacExe -version >> $javaLog

Set-Location 'c:\planora\PlanningService\planning-service'
& .\mvnw -q -DskipTests compile *> $compileLog
if ($LASTEXITCODE -ne 0) {
  throw "Maven compile failed. See $compileLog"
}
Write-Host 'COMPILE_OK'
