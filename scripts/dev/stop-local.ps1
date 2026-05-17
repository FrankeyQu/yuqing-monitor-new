$ErrorActionPreference = "SilentlyContinue"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$Tools = Join-Path $Root ".codex-tools"

Get-CimInstance Win32_Process -Filter "name='java.exe'" |
    Where-Object { $_.CommandLine -like "*$Root*" -and $_.CommandLine -like "*stonedt-portal-0.5.3-SNAPSHOT.jar*" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

Get-CimInstance Win32_Process -Filter "name='mariadbd.exe'" |
    Where-Object { $_.CommandLine -like "*$Tools*" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

Get-CimInstance Win32_Process -Filter "name='redis-server.exe'" |
    Where-Object { $_.CommandLine -like "*$Tools*" -or $_.CommandLine -like "*6379*" } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }

Write-Host "Local Campus Yuqing processes stopped."
