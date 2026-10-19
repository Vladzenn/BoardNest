$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$wrapper = Join-Path $projectRoot 'mvnw.cmd'
$dbName = 'boarding_house_db'
$dbUrl = 'jdbc:mysql://localhost:3306/boarding_house_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'

function Test-TcpPort {
    param([string]$HostName, [int]$Port, [int]$TimeoutMs = 1500)
    $client = $null
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $result = $client.BeginConnect($HostName, $Port, $null, $null)
        if (-not $result.AsyncWaitHandle.WaitOne($TimeoutMs)) { return $false }
        $client.EndConnect($result)
        return $true
    } catch { return $false }
    finally { if ($client) { $client.Close() } }
}

function Wait-ForMySql {
    param([int]$Seconds = 60)
    for ($second = 0; $second -lt $Seconds; $second++) {
        if (Test-TcpPort -HostName '127.0.0.1' -Port 3306) { return $true }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Find-MySqlClient {
    $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    $candidates = @()
    if ($env:LARAGON_PATH) {
        $candidates += (Join-Path $env:LARAGON_PATH 'bin\mysql')
        $candidates += (Join-Path $env:LARAGON_PATH 'bin\mariadb')
    }
    $candidates += @('C:\laragon\bin\mysql', 'C:\laragon\bin\mariadb', 'D:\laragon\bin\mysql', 'D:\laragon\bin\mariadb')
    foreach ($base in $candidates) {
        if (Test-Path $base) {
            $direct = Join-Path $base 'bin\mysql.exe'
            if (Test-Path $direct) { return $direct }
            $versions = Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending
            foreach ($version in $versions) {
                $candidate = Join-Path $version.FullName 'bin\mysql.exe'
                if (Test-Path $candidate) { return $candidate }
            }
        }
    }
    $programFiles = @($env:ProgramFiles, ${env:ProgramFiles(x86)}) | Where-Object { $_ }
    foreach ($root in $programFiles) {
        foreach ($base in @((Join-Path $root 'MySQL\MySQL Server*'), (Join-Path $root 'MariaDB *'))) {
            foreach ($folder in (Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue)) {
                $candidate = Join-Path $folder.FullName 'bin\mysql.exe'
                if (Test-Path $candidate) { return $candidate }
            }
        }
    }
    return $null
}

function ConvertTo-SqlLiteralContent {
    param([string]$Value)
    # Escape backslashes and single quotes for a MySQL string literal.
    return $Value.Replace('\', '\\').Replace("'", "''")
}

function Invoke-MySqlQuery {
    param([string]$User, [string]$Password, [string]$Sql)
    $oldPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $Password
        $output = @(& $script:mysqlExe --protocol=TCP --host=127.0.0.1 --port=3306 --user=$User --batch --skip-column-names --execute=$Sql 2>&1)
        $code = $LASTEXITCODE
        return [pscustomobject]@{ ExitCode = $code; Output = ($output -join "`n") }
    } finally {
        $env:MYSQL_PWD = $oldPassword
    }
}

function Invoke-MySqlScript {
    param([string]$User, [string]$Password, [string]$ScriptText)
    $oldPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $Password
        $output = @($ScriptText | & $script:mysqlExe --protocol=TCP --host=127.0.0.1 --port=3306 --user=$User 2>&1)
        $code = $LASTEXITCODE
        return [pscustomobject]@{ ExitCode = $code; Output = ($output -join "`n") }
    } finally {
        $env:MYSQL_PWD = $oldPassword
    }
}

function SecureString-ToPlainText {
    param([Security.SecureString]$SecureString)
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureString)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
}

Write-Host ''
Write-Host '=====================================' -ForegroundColor Green
Write-Host '       BoardNest Easy Launcher' -ForegroundColor Green
Write-Host '=====================================' -ForegroundColor Green
Write-Host "Project folder: $projectRoot"
Write-Host ''

if (-not (Test-Path $wrapper) -or -not (Test-Path (Join-Path $projectRoot 'pom.xml'))) {
    Write-Host 'ERROR: Required project files are missing. Extract the complete BoardNest ZIP first.' -ForegroundColor Red
    exit 1
}
Write-Host 'Project files: OK' -ForegroundColor Green

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCommand) {
    Write-Host 'ERROR: Java was not found. Install JDK 21 and make sure java is on PATH.' -ForegroundColor Red
    exit 1
}
$javaVersionText = (& $env:ComSpec /d /c 'java -version 2>&1' | Out-String)
if ($javaVersionText -notmatch 'version "(?:1\.)?(\d+)') {
    Write-Host 'ERROR: Could not determine the Java version. Check your Java installation.' -ForegroundColor Red
    exit 1
}
$javaMajor = [int]$Matches[1]
if ($javaMajor -ne 21) {
    Write-Host "ERROR: BoardNest requires Java 21. Detected Java $javaMajor." -ForegroundColor Red
    exit 1
}
Write-Host 'Java 21: OK' -ForegroundColor Green

if (-not (Test-TcpPort -HostName '127.0.0.1' -Port 3306)) {
    Write-Host 'MySQL is not running on 127.0.0.1:3306. Trying to start it...' -ForegroundColor Yellow
    $startedService = $false
    $mysqlServices = @(Get-Service -ErrorAction SilentlyContinue | Where-Object {
        $_.Name -match '^(MySQL|MySQL\d+|MariaDB|mariadb|mysql.*)$' -or $_.DisplayName -match 'MySQL|MariaDB'
    })
    foreach ($service in $mysqlServices) {
        if ($service.Status -ne 'Running') {
            try {
                Start-Service -Name $service.Name -ErrorAction Stop
                Write-Host "Started database service: $($service.DisplayName)" -ForegroundColor Green
                $startedService = $true
                break
            } catch { Write-Host "Could not start '$($service.DisplayName)' automatically; it may require Administrator permission." -ForegroundColor DarkYellow }
        }
    }
    if (-not (Test-TcpPort -HostName '127.0.0.1' -Port 3306)) {
        $laragonCandidates = @()
        if ($env:LARAGON_PATH) { $laragonCandidates += (Join-Path $env:LARAGON_PATH 'laragon.exe') }
        $laragonCandidates += @('C:\laragon\laragon.exe', 'D:\laragon\laragon.exe', (Join-Path $env:LOCALAPPDATA 'Programs\Laragon\laragon.exe'))
        $laragonExe = $laragonCandidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
        if ($laragonExe) {
            try { Start-Process -FilePath $laragonExe | Out-Null; Write-Host 'Opened Laragon; waiting for MySQL...' -ForegroundColor Yellow }
            catch { Write-Host 'Laragon was found but could not be opened automatically.' -ForegroundColor DarkYellow }
        }
        if (-not (Wait-ForMySql -Seconds 45)) {
            Write-Host 'Start MySQL in Laragon or your MySQL app, then press Enter.' -ForegroundColor Yellow
            Read-Host 'Press Enter after starting MySQL'
            if (-not (Wait-ForMySql -Seconds 30)) {
                Write-Host 'ERROR: MySQL is still not reachable at 127.0.0.1:3306.' -ForegroundColor Red
                exit 1
            }
        }
    }
}
Write-Host 'MySQL port 3306: reachable' -ForegroundColor Green

$script:mysqlExe = Find-MySqlClient
if (-not $script:mysqlExe) {
    Write-Host 'ERROR: The MySQL command-line client (mysql.exe) was not found.' -ForegroundColor Red
    Write-Host 'Install/use Laragon or add its bin\mysql\...\bin folder to PATH. The launcher needs mysql.exe to create/check tables.' -ForegroundColor Yellow
    exit 1
}
Write-Host "MySQL client: $script:mysqlExe" -ForegroundColor Green

$dbUser = Read-Host 'MySQL application username [boarding_house_app]'
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'boarding_house_app' }
if ($dbUser -notmatch '^[A-Za-z0-9_]+$') {
    Write-Host 'ERROR: Use only letters, numbers, and underscores for the database username.' -ForegroundColor Red
    exit 1
}
$securePassword = Read-Host 'MySQL application password (input hidden)' -AsSecureString
$dbPassword = SecureString-ToPlainText $securePassword
if ([string]::IsNullOrEmpty($dbPassword)) {
    Write-Host 'ERROR: The application password cannot be empty.' -ForegroundColor Red
    $securePassword.Dispose()
    exit 1
}

try {
    Write-Host ''
    Write-Host 'Checking the database account and schema...' -ForegroundColor Cyan
    $loginCheck = Invoke-MySqlQuery -User $dbUser -Password $dbPassword -Sql 'SELECT 1;'
    $needsAdminSetup = ($loginCheck.ExitCode -ne 0)
    if (-not $needsAdminSetup) {
        $databaseCheck = Invoke-MySqlQuery -User $dbUser -Password $dbPassword -Sql "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='$dbName';"
        if ($databaseCheck.ExitCode -ne 0 -or $databaseCheck.Output.Trim() -ne '1') { $needsAdminSetup = $true }
    }

    if ($needsAdminSetup) {
        Write-Host 'The application account or database is missing, or the supplied app credentials were rejected.' -ForegroundColor Yellow
        Write-Host 'One-time setup needs a MySQL administrator account (often root in a local Laragon install).' -ForegroundColor Yellow
        $adminUser = Read-Host 'MySQL administrator username [root]'
        if ([string]::IsNullOrWhiteSpace($adminUser)) { $adminUser = 'root' }
        $secureAdminPassword = Read-Host 'MySQL administrator password (input hidden; blank is allowed if local root has no password)' -AsSecureString
        $adminPassword = SecureString-ToPlainText $secureAdminPassword
        try {
            $escapedDbPassword = ConvertTo-SqlLiteralContent $dbPassword
            $sql = @"
CREATE DATABASE IF NOT EXISTS $dbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$dbUser'@'localhost' IDENTIFIED BY '$escapedDbPassword';
ALTER USER '$dbUser'@'localhost' IDENTIFIED BY '$escapedDbPassword';
CREATE USER IF NOT EXISTS '$dbUser'@'127.0.0.1' IDENTIFIED BY '$escapedDbPassword';
ALTER USER '$dbUser'@'127.0.0.1' IDENTIFIED BY '$escapedDbPassword';
GRANT ALL PRIVILEGES ON ${dbName}.* TO '$dbUser'@'localhost';
GRANT ALL PRIVILEGES ON ${dbName}.* TO '$dbUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
            $adminResult = Invoke-MySqlScript -User $adminUser -Password $adminPassword -ScriptText $sql
            if ($adminResult.ExitCode -ne 0) {
                Write-Host 'ERROR: Database setup failed. Check the administrator username/password and privileges.' -ForegroundColor Red
                Write-Host $adminResult.Output -ForegroundColor DarkRed
                exit 1
            }
            Write-Host 'Database and application account: ready' -ForegroundColor Green
        } finally {
            $adminPassword = $null
            $secureAdminPassword.Dispose()
        }
    } else {
        Write-Host 'Database account and database: found' -ForegroundColor Green
    }

    $schemaFile = Join-Path $projectRoot 'src\main\resources\db\manual\schema.sql'
    $migrationFile = Join-Path $projectRoot 'src\main\resources\db\manual\migration_add_tenant_monthly_rent.sql'
    if (-not (Test-Path $schemaFile)) {
        Write-Host "ERROR: Schema file not found: $schemaFile" -ForegroundColor Red
        exit 1
    }

    # CREATE TABLE IF NOT EXISTS is safe for existing tables; it does not drop data.
    $schemaText = Get-Content -LiteralPath $schemaFile -Raw
    $schemaResult = Invoke-MySqlScript -User $dbUser -Password $dbPassword -ScriptText $schemaText
    if ($schemaResult.ExitCode -ne 0) {
        Write-Host 'ERROR: Could not create/check the database tables with the application account.' -ForegroundColor Red
        Write-Host $schemaResult.Output -ForegroundColor DarkRed
        Write-Host 'If this is a restricted existing account, rerun setup with an administrator account able to grant database privileges.' -ForegroundColor Yellow
        exit 1
    }

    $rentCheck = Invoke-MySqlQuery -User $dbUser -Password $dbPassword -Sql "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='$dbName' AND TABLE_NAME='tenants' AND COLUMN_NAME='monthly_rent';"
    if ($rentCheck.ExitCode -ne 0) {
        Write-Host 'ERROR: Could not verify the tenants table.' -ForegroundColor Red
        Write-Host $rentCheck.Output -ForegroundColor DarkRed
        exit 1
    }
    if ($rentCheck.Output.Trim() -ne '1') {
        if (-not (Test-Path $migrationFile)) {
            Write-Host 'ERROR: monthly_rent is missing and the migration file was not found.' -ForegroundColor Red
            exit 1
        }
        Write-Host 'Applying the missing monthly_rent column migration...' -ForegroundColor Yellow
        $migrationText = Get-Content -LiteralPath $migrationFile -Raw
        $migrationResult = Invoke-MySqlScript -User $dbUser -Password $dbPassword -ScriptText $migrationText
        if ($migrationResult.ExitCode -ne 0) {
            Write-Host 'ERROR: The monthly rent migration failed. Review the SQL error below.' -ForegroundColor Red
            Write-Host $migrationResult.Output -ForegroundColor DarkRed
            exit 1
        }
    }

    $tableCheck = Invoke-MySqlQuery -User $dbUser -Password $dbPassword -Sql "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$dbName' AND TABLE_NAME IN ('users','properties','floors','rooms','tenants','occupancies');"
    if ($tableCheck.ExitCode -ne 0 -or $tableCheck.Output.Trim() -ne '6') {
        Write-Host 'ERROR: Not all required BoardNest tables are present. Check the schema SQL and MySQL permissions.' -ForegroundColor Red
        Write-Host $tableCheck.Output -ForegroundColor DarkRed
        exit 1
    }
    Write-Host 'Database schema and required tables: OK' -ForegroundColor Green

    $env:DB_URL = $dbUrl
    $env:DB_USERNAME = $dbUser
    $env:DB_PASSWORD = $dbPassword
    Write-Host ''
    Write-Host 'Starting BoardNest. Maven may download dependencies on the first run.' -ForegroundColor Green
    Write-Host 'Do not close this window while using the website. Press Ctrl+C to stop it.' -ForegroundColor Yellow
    $commandLine = '/d /s /c ""{0}" spring-boot:run"' -f $wrapper
    $process = Start-Process -FilePath $env:ComSpec -ArgumentList $commandLine -WorkingDirectory $projectRoot -PassThru -NoNewWindow
    # Keep credentials available to the child process only until startup has been initiated.
    $env:DB_PASSWORD = $null
    $dbPassword = $null
    $securePassword.Dispose()

    $ready = $false
    for ($i = 0; $i -lt 120; $i++) {
        Start-Sleep -Seconds 1
        if ($process.HasExited) {
            Write-Host 'The application process stopped before becoming ready. Check the Maven/MySQL error above.' -ForegroundColor Red
            exit 1
        }
        try {
            $null = Invoke-WebRequest -Uri 'http://localhost:8080' -TimeoutSec 2 -UseBasicParsing
            $ready = $true
            break
        } catch { if ($_.Exception.Response) { $ready = $true; break } }
    }
    if ($ready) {
        Write-Host 'BoardNest is responding. Opening http://localhost:8080' -ForegroundColor Green
        Start-Process 'http://localhost:8080'
    } else {
        Write-Host 'The app has not responded within 120 seconds. Check the logs above.' -ForegroundColor Yellow
    }
    $process.WaitForExit()
    exit $process.ExitCode
} finally {
    $env:DB_PASSWORD = $null
    $dbPassword = $null
    if ($securePassword) { $securePassword.Dispose() }
}
