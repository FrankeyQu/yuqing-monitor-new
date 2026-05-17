param(
    [int]$MysqlPort = 3306,
    [string]$Database = "stonedt_portal"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$MariaBase = Get-ChildItem -Path (Join-Path $Root ".codex-tools\mariadb") -Directory | Select-Object -First 1 -ExpandProperty FullName
$Mysql = Join-Path $MariaBase "bin\mysql.exe"
if (-not (Test-Path $Mysql)) {
    $Mysql = Join-Path $MariaBase "bin\mariadb.exe"
}
if (-not (Test-Path $Mysql)) {
    throw "mysql.exe or mariadb.exe not found under $MariaBase\bin"
}

$Sql = Join-Path $PSScriptRoot "seed-campus-demo.sql"
if (-not (Test-Path $Sql)) {
    throw "Seed SQL not found: $Sql"
}

$SqlForMysql = (Resolve-Path $Sql).Path.Replace("\", "/")

& $Mysql `
    --user=root `
    --password=123456 `
    --host=127.0.0.1 `
    --port=$MysqlPort `
    --protocol=tcp `
    --default-character-set=utf8mb4 `
    $Database `
    --execute="source $SqlForMysql"

Write-Host "Campus demo data seeded into database '$Database'."
