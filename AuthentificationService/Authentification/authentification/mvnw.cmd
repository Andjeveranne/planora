<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.4
@REM ----------------------------------------------------------------------------

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
if (!$distributionUrl) { Write-Error "cannot read distributionUrl property in $scriptDir/.mvn/wrapper/maven-wrapper.properties" }
$USE_MVND = $false; $MVN_CMD = "mvn"
if ($distributionUrl -match 'maven-mvnd-') { $USE_MVND = $true; $distributionUrl = $distributionUrl -replace '-bin\.[^.]*$',"-windows-amd64.zip"; $MVN_CMD = "mvnd.cmd" }
if ($env:MVNW_REPOURL) { $p = if ($USE_MVND) { "/maven/mvnd/" } else { "/org/apache/maven/" }; $distributionUrl = "$env:MVNW_REPOURL$p$($distributionUrl -replace "^.*$p",'')" }
$distributionUrlName = $distributionUrl -replace '^.*/',''
$distributionUrlNameMain = $distributionUrlName -replace '\.[^.]*$','' -replace '-bin$',''
$MAVEN_M2_PATH = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { "$HOME/.m2" }
if (-not (Test-Path -Path $MAVEN_M2_PATH)) { New-Item -Path $MAVEN_M2_PATH -ItemType Directory | Out-Null }
$MAVEN_WRAPPER_DISTS = if ((Get-Item $MAVEN_M2_PATH).Target[0]) { (Get-Item $MAVEN_M2_PATH).Target[0] + "/wrapper/dists" } else { "$MAVEN_M2_PATH/wrapper/dists" }
$MAVEN_HOME_PARENT = "$MAVEN_WRAPPER_DISTS/$distributionUrlNameMain"
$MAVEN_HOME_NAME = ([System.Security.Cryptography.SHA256]::Create().ComputeHash([byte[]][char[]]$distributionUrl) | ForEach-Object {$_.ToString("x2")}) -join ''
$MAVEN_HOME = "$MAVEN_HOME_PARENT/$MAVEN_HOME_NAME"
if (Test-Path -Path "$MAVEN_HOME" -PathType Container) { Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"; exit 0 }
if (! $distributionUrlNameMain -or ($distributionUrlName -eq $distributionUrlNameMain)) { Write-Error "distributionUrl is not valid, must end with *-bin.zip" }
$TMP_DOWNLOAD_DIR_HOLDER = New-TemporaryFile; $TMP_DOWNLOAD_DIR = New-Item -Itemtype Directory -Path "$TMP_DOWNLOAD_DIR_HOLDER.dir"; $TMP_DOWNLOAD_DIR_HOLDER.Delete() | Out-Null
New-Item -Itemtype Directory -Path "$MAVEN_HOME_PARENT" -Force | Out-Null
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$wc = New-Object System.Net.WebClient; if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) { $wc.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD) }; $wc.DownloadFile($distributionUrl, "$TMP_DOWNLOAD_DIR/$distributionUrlName") | Out-Null
Expand-Archive "$TMP_DOWNLOAD_DIR/$distributionUrlName" -DestinationPath "$TMP_DOWNLOAD_DIR" | Out-Null
$actualDistributionDir = ""; $expectedPath = Join-Path "$TMP_DOWNLOAD_DIR" "$distributionUrlNameMain"; if ((Test-Path -Path $expectedPath -PathType Container) -and (Test-Path -Path "$expectedPath/bin/$MVN_CMD" -PathType Leaf)) { $actualDistributionDir = $distributionUrlNameMain }
if (!$actualDistributionDir) { Get-ChildItem -Path "$TMP_DOWNLOAD_DIR" -Directory | ForEach-Object { if (Test-Path -Path "$($_.FullName)/bin/$MVN_CMD" -PathType Leaf) { $actualDistributionDir = $_.Name; break } } }
if (!$actualDistributionDir) { Write-Error "Could not find Maven distribution directory" }
Rename-Item -Path "$TMP_DOWNLOAD_DIR/$actualDistributionDir" -NewName $MAVEN_HOME_NAME | Out-Null
try { Move-Item -Path "$TMP_DOWNLOAD_DIR/$MAVEN_HOME_NAME" -Destination $MAVEN_HOME_PARENT | Out-Null } catch { if (! (Test-Path -Path "$MAVEN_HOME" -PathType Container)) { Write-Error "fail to move MAVEN_HOME" } }
try { Remove-Item $TMP_DOWNLOAD_DIR -Recurse -Force | Out-Null } catch {}
Write-Output "MVN_CMD=$MAVEN_HOME/bin/$MVN_CMD"
