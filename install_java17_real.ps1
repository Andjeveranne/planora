$ErrorActionPreference = 'Stop'

function Find-Java17 {
    $roots = @(
        'C:\Program Files\Microsoft',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Java',
        'C:\Tools'
    )

    foreach ($root in $roots) {
        if (-not (Test-Path $root)) { continue }
        $dirs = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue
        foreach ($dir in $dirs) {
            $javaExe = Join-Path $dir.FullName 'bin\java.exe'
            if (Test-Path $javaExe) {
                $ver = & $javaExe -version 2>&1 | Out-String
                if ($ver -match 'version "17\.') {
                    return $dir.FullName
                }
            }
        }
    }

    return $null
}

$javaHome = Find-Java17
if (-not $javaHome) {
    New-Item -ItemType Directory -Force -Path 'C:\Tools' | Out-Null
    $zip = 'C:\Tools\jdk17.zip'
    $url = 'https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip'
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $url -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath 'C:\Tools' -Force
    $javaHome = Find-Java17
}

if (-not $javaHome) {
    throw 'Java 17 could not be installed or detected.'
}

$javaBin = Join-Path $javaHome 'bin'
[Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($userPath -notmatch [regex]::Escape($javaBin)) {
    $newPath = if ([string]::IsNullOrWhiteSpace($userPath)) { $javaBin } else { $userPath.TrimEnd(';') + ';' + $javaBin }
    [Environment]::SetEnvironmentVariable('Path', $newPath, 'User')
}

$env:JAVA_HOME = $javaHome
$env:Path = $javaBin + ';' + $env:Path

Write-Host "JAVA_HOME=$javaHome"
& (Join-Path $javaHome 'bin\java.exe') -version 2>&1
