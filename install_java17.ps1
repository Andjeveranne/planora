$ErrorActionPreference = 'Stop'
$archiveUrl = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16%2B8/OpenJDK17U-jdk_x64_windows_hotspot_17.0.16_8.zip'
$downloadPath = 'C:\planora\temurin17.zip'
$installRoot = 'C:\Program Files\Eclipse Adoptium'
$javaRoot = Join-Path $installRoot 'jdk-17.0.16.8-hotspot'

New-Item -ItemType Directory -Path $installRoot -Force | Out-Null
if (-not (Test-Path $javaRoot\bin\java.exe)) {
    if (Test-Path $downloadPath) { Remove-Item $downloadPath -Force }
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $archiveUrl -OutFile $downloadPath
    Expand-Archive -Path $downloadPath -DestinationPath $installRoot -Force
}

$javaExe = Join-Path $javaRoot 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    throw "Java 17 was not installed successfully: $javaExe"
}

& $javaExe -version
Write-Host "JAVA17_OK:$javaRoot"
