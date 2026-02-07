<# : batch portion
@REM Apache Maven Wrapper 3.3.4
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; icm -ScriptBlock ([Scriptblock]::Create((Get-Content -Raw '%~f0'))) -NoNewScope}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_ARG0_NAME__=
@IF NOT "%__MVNW_CMD__%"=="" ("%__MVNW_CMD__%" %*)
@echo Cannot start maven from wrapper >&2 && exit /b 1
@GOTO :EOF
: end batch / begin powershell #>
$ErrorActionPreference = "Stop"
if ($env:MVNW_VERBOSE -eq "true") { $VerbosePreference = "Continue" }
$distributionUrl = (Get-Content -Raw "$scriptDir/.mvn/wrapper/maven-wrapper.properties" | ConvertFrom-StringData).distributionUrl
if (!$distributionUrl) { Write-Error "cannot read distributionUrl" }
$USE_MVND = $false; $MVN_CMD = "mvn"
if ($distributionUrl -match 'maven-mvnd-') { $USE_MVND = $true; $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$',"-windows-amd64.zip"; $MVN_CMD = "mvnd.cmd" }
if ($env:MVNW_REPOURL) { $p = if ($USE_MVND) { "/maven/mvnd/" } else { "/org/apache/maven/" }; $distributionUrl = "$env:MVNW_REPOURL$p$($distributionUrl -replace "^.*$p",'')" }
$distributionUrlName = $distributionUrl -replace '^.*/',''; $distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''
$MAVEN_M2_PATH = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { "$HOME/.m2" }
if (-not (Test-Path -Path $MAVEN_M2_PATH)) { New-Item -Path $MAVEN_M2_PATH -ItemType Directory | Out-Null }
$MAVEN_WRAPPER_DISTS = if ((Get-Item $MAVEN_M2_PATH).Target[0]) { (Get-Item $MAVEN_M2_PATH).Target[0] + "/wrapper/dists" } else { "$MAVEN_M2_PATH/wrapper/dists" }
$MAVEN_HOME_PARENT = "$MAVEN_WRAPPER_DISTS/$distributionUrlNameMain"; $MAVEN_HOME_NAME = ([System.Security.Cryptography.SHA256]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''; $MAVEN_HOME = "$MAVEN_HOME_PARENT/$MAVEN_HOME_NAME"
if (Test-Path -Path "$MAVEN_HOME" -PathType Container) { Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"; exit 0 }
$TMP_DOWNLOAD_DIR_HOLDER = New-TemporaryFile; $TMP_DOWNLOAD_DIR = New-Item -Itemtype Directory -Path "$TMP_DOWNLOAD_DIR_HOLDER.dir"; $TMP_DOWNLOAD_DIR_HOLDER.Delete() | Out-Null
New-Item -Itemtype Directory -Path "$MAVEN_HOME_PARENT" -Force | Out-Null
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile($distributionUrl, "$TMP_DOWNLOAD_DIR/$distributionUrlName"); Expand-Archive "$TMP_DOWNLOAD_DIR/$distributionUrlName" -DestinationPath "$TMP_DOWNLOAD_DIR" | Out-Null
$actualDistributionDir = ""; if ((Test-Path "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" -PathType Container) -and (Test-Path "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain/bin/$MVN_CMD" -PathType Leaf)) { $actualDistributionDir = $distributionUrlNameMain }; if (!$actualDistributionDir) { Get-ChildItem -Path "$TMP_DOWNLOAD_DIR" -Directory | ForEach-Object { if (Test-Path "$($_.FullName)/bin/$MVN_CMD" -PathType Leaf) { $actualDistributionDir = $_.Name; break } } }
if (!$actualDistributionDir) { Write-Error "Could not find Maven in archive" }; Rename-Item -Path "$TMP_DOWNLOAD_DIR/$actualDistributionDir" -NewName $MAVEN_HOME_NAME | Out-Null; Move-Item -Path "$TMP_DOWNLOAD_DIR/$MAVEN_HOME_NAME" -Destination $MAVEN_HOME_PARENT | Out-Null; try { Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force } catch {}; Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
