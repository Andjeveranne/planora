$ErrorActionPreference = 'Stop'

$installRoot = 'C:\Program Files\Eclipse Adoptium'

function Get-ExistingJdk17 {
    $candidates = @(
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Java',
        'C:\Tools'
    )

    foreach ($root in $candidates) {
        if (-not (Test-Path $root)) { continue }
        $matches = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match 'jdk-17|Temurin.*17|jdk.*17' }
        if ($matches) {
            $jdk = $matches | Select-Object -First 1
            $javaExe = Join-Path $jdk.FullName 'bin\java.exe'
            if (Test-Path $javaExe) { return $jdk.FullName }
        }
    }

    return $null
}

$javaHome = Get-ExistingJdk17
if (-not $javaHome) {
    if (Get-Command winget -ErrorAction SilentlyContinue) {
        Write-Host 'Installing Temurin JDK 17 with winget...'
        winget install --id EclipseAdoptium.Temurin.17.JDK --source winget -e --accept-source-agreements --accept-package-agreements
        $javaHome = Get-ExistingJdk17
    }
}

if (-not $javaHome) {
    Write-Host 'No Java 17 found; downloading Temurin JDK 17 archive...'
    $destDir = 'C:\Tools'
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    $zipPath = Join-Path $destDir 'jdk17.zip'
    $url = 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16.8%2B8/OpenJDK17U-jdk_x64_windows_hotspot_17.0.16.8_8.zip'
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $destDir -Force
    $javaHome = Get-ExistingJdk17
}

if (-not $javaHome) {
    throw 'Java 17 could not be installed or detected.'
}

$javaExe = Join-Path $javaHome 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    throw "Java executable not found at $javaExe"
}

[Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($userPath -notmatch [regex]::Escape((Join-Path $javaHome 'bin'))) {
    [Environment]::SetEnvironmentVariable('Path', ($userPath.TrimEnd(';') + ';' + (Join-Path $javaHome 'bin')), 'User')
}

$env:JAVA_HOME = $javaHome
$env:Path = (Join-Path $javaHome 'bin') + ';' + $env:Path

Write-Host "JAVA_HOME=$javaHome"
& $javaExe -version
