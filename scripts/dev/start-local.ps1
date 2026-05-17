param(
    [int]$AppPort = 8084,
    [int]$MysqlPort = 3306,
    [int]$RedisPort = 6379
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$Tools = Join-Path $Root ".codex-tools"
$JdkHome = Get-ChildItem -Path (Join-Path $Tools "jdk8") -Directory | Select-Object -First 1 -ExpandProperty FullName
$MariaBase = Get-ChildItem -Path (Join-Path $Tools "mariadb") -Directory | Select-Object -First 1 -ExpandProperty FullName
$MariaData = Join-Path $Tools "mariadb-data"
$RedisServer = Join-Path $Tools "redis\redis-server.exe"
$Jar = Join-Path $Root "target\stonedt-portal-0.5.3-SNAPSHOT.jar"
$AppLog = Join-Path $Tools "app.log"
$AppErrLog = Join-Path $Tools "app.err.log"

function Test-PortOpen {
    param([int]$Port)
    $client = New-Object Net.Sockets.TcpClient
    try {
        $iar = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        if (-not $iar.AsyncWaitHandle.WaitOne(1000, $false)) {
            return $false
        }
        $client.EndConnect($iar)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

if (-not (Test-Path $JdkHome)) {
    throw "JDK 8 not found under $Tools\jdk8. Run the Codex bootstrap steps first."
}
if (-not (Test-Path $MariaBase)) {
    throw "MariaDB not found under $Tools\mariadb. Run the Codex bootstrap steps first."
}
if (-not (Test-Path $RedisServer)) {
    throw "Redis not found at $RedisServer. Run the Codex bootstrap steps first."
}
if (-not (Test-Path $Jar)) {
    Push-Location $Root
    try {
        $env:JAVA_HOME = $JdkHome
        $env:Path = "$env:JAVA_HOME\bin;$env:Path"
        & .\mvnw.cmd -DskipTests package
    } finally {
        Pop-Location
    }
}

if (-not (Test-PortOpen -Port $MysqlPort)) {
    $mysqld = Join-Path $MariaBase "bin\mariadbd.exe"
    Start-Process -FilePath $mysqld `
        -ArgumentList @("--defaults-file=$MariaData\my.ini", "--datadir=$MariaData", "--port=$MysqlPort", "--console") `
        -WindowStyle Hidden
}

for ($i = 1; $i -le 60 -and -not (Test-PortOpen -Port $MysqlPort); $i++) {
    Start-Sleep -Seconds 1
}
if (-not (Test-PortOpen -Port $MysqlPort)) {
    throw "MariaDB did not open port $MysqlPort."
}

$mysql = Join-Path $MariaBase "bin\mariadb.exe"
& $mysql --user=root --password=123456 --host=127.0.0.1 --port=$MysqlPort --protocol=tcp `
    -e "CREATE DATABASE IF NOT EXISTS stonedt_portal DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

if (-not (Test-PortOpen -Port $RedisPort)) {
    Start-Process -FilePath $RedisServer -ArgumentList @("--port", "$RedisPort") -WindowStyle Hidden
}
for ($i = 1; $i -le 30 -and -not (Test-PortOpen -Port $RedisPort); $i++) {
    Start-Sleep -Seconds 1
}
if (-not (Test-PortOpen -Port $RedisPort)) {
    throw "Redis did not open port $RedisPort."
}

if (-not (Test-PortOpen -Port $AppPort)) {
    $java = Join-Path $JdkHome "bin\java.exe"
    Start-Process -FilePath $java `
        -ArgumentList @("-jar", $Jar) `
        -WorkingDirectory $Root `
        -RedirectStandardOutput $AppLog `
        -RedirectStandardError $AppErrLog `
        -WindowStyle Hidden
}

for ($i = 1; $i -le 60 -and -not (Test-PortOpen -Port $AppPort); $i++) {
    Start-Sleep -Seconds 1
}
if (-not (Test-PortOpen -Port $AppPort)) {
    throw "Application did not open port $AppPort. Check $AppLog and $AppErrLog."
}

Write-Host "Campus Yuqing is running at http://127.0.0.1:$AppPort/"
Write-Host "Logs: $AppLog"
