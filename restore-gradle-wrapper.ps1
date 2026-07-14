$ErrorActionPreference = "Stop"
$wrapperDir = Join-Path $PSScriptRoot "gradle\wrapper"
$base64Path = Join-Path $wrapperDir "gradle-wrapper.jar.base64"
$jarPath = Join-Path $wrapperDir "gradle-wrapper.jar"
[System.IO.File]::WriteAllBytes($jarPath, [System.Convert]::FromBase64String((Get-Content $base64Path -Raw)))
Write-Host "Restored $jarPath"
