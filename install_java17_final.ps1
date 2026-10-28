$ErrorActionPreference = 'Stop'
$logPath = 'C:\planora\java17_install.log'
"[start] $(Get-Date -Format o)" | Tee-Object -FilePath $logPath -Append | Out-Null

function Add-Log($msg) {
    $stamp = (Get-Date -Format o)
    "[$stamp] $msg" | Tee-Object -FilePath $logPath -Append | Out-Null
}

$msiUrl = 'https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.msi'
$msiPath = 'C:\Tools\microsoft-jdk-17.msi'
$javaCandidates = @(
    'C:\Program Files\Microsoft\jdk-17',
    'C:\Program Files\Microsoft\jdk-17.0.16.8-hotspot',
    'C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot',
    'C:\Program Files\Java\jdk-17'
)

try {
    foreach ($cand in $javaCandidates) {
        $javaExe = Join-Path $cand 'bin\java.exe'
        if (Test-Path $javaExe) {
            Add-Log "Existing Java 17 found at $cand"
            $javaHome = $cand
            goto :FoundJava
        }
    }

    New-Item -ItemType Directory -Force -Path 'C:\Tools' | Out-Null
    Add-Log "Downloading Java 17 MSI..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $msiUrl -OutFile $msiPath
    Add-Log "MSI downloaded to $msiPath"

    $installProc = Start-Process -FilePath 'msiexec.exe' -ArgumentList '/i', $msiPath, '/qn' -Wait -PassThru
    Add-Log "msiexec exited with code $($installProc.ExitCode)"

    foreach ($cand in $javaCandidates) {
        $javaExe = Join-Path $cand 'bin\java.exe'
        if (Test-Path $javaExe) {
            Add-Log "Java 17 found after install at $cand"
            $javaHome = $cand
            goto :FoundJava
        }
    }

    throw 'Java 17 was not found after MSI install.'
}
catch {
    Add-Log "ERROR: $($_.Exception.Message)"
    throw
}

:FoundJava
$env:JAVA_HOME = $javaHome
$env:Path = (Join-Path $javaHome 'bin') + ';' + $env:Path
[Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'User')
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$javaBin = Join-Path $javaHome 'bin'
if ($userPath -notmatch [regex]::Escape($javaBin)) {
    $newUserPath = $userPath.TrimEnd(';') + ';' + $javaBin
    [Environment]::SetEnvironmentVariable('Path', $newUserPath, 'User')
}

Add-Log "JAVA_HOME set to $javaHome"
& (Join-Path $javaHome 'bin\java.exe') -version 2>&1 | Tee-Object -FilePath $logPath -Append | Out-Null
Add-Log "Setup complete"
