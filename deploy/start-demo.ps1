param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'demo-common.ps1')

& docker info --format '{{.ServerVersion}}' | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Start Docker Desktop and wait for the Linux engine, then run this script again.' }

if (-not (Test-Path -LiteralPath $DemoEnvPath)) {
    $source = Read-DemoEnv -Path (Join-Path $DemoRoot '.env')
    foreach ($key in @('OPENAI_API_KEY', 'DASHSCOPE_API_KEY')) {
        if (-not $source.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($source[$key])) { throw "Configure $key in .env first." }
    }
    $entrancePassword = New-DemoSecret -Bytes 12
    $appPassword = New-DemoSecret -Bytes 12
    $hashOutput = & docker run --rm caddy:2 caddy hash-password --plaintext $entrancePassword
    if ($LASTEXITCODE -ne 0) { throw 'Unable to generate the Caddy password hash.' }
    $hash = ($hashOutput | Where-Object { $_ -match '^\$2[aby]\$' } | Select-Object -Last 1)
    if (-not $hash) { throw 'Caddy did not return a bcrypt password hash.' }
    $lines = @(
        '# Generated for the separate local public demo. Never commit this file.'
        "MYSQL_ROOT_PASSWORD=$(New-DemoSecret)"
        "MYSQL_APP_PASSWORD=$(New-DemoSecret)"
        "JWT_ADMIN_SECRET=$(New-DemoSecret -Bytes 32)"
        "JWT_USER_SECRET=$(New-DemoSecret -Bytes 32)"
        "OPENAI_API_KEY=$($source['OPENAI_API_KEY'])"
        "DASHSCOPE_API_KEY=$($source['DASHSCOPE_API_KEY'])"
        "ADMIN_APP_PASSWORD=$appPassword"
        'ADMIN_BASIC_AUTH_USER=demo-admin'
        "ADMIN_BASIC_AUTH_PASSWORD=$entrancePassword"
        "ADMIN_BASIC_AUTH_HASH='$hash'"
    )
    New-Item -ItemType Directory -Path $DemoStatePath -Force | Out-Null
    Write-DemoFile -Path $DemoEnvPath -Content (($lines -join "`n") + "`n")
    $credentials = @(
        'Delivo demo credentials - private, never commit or share with user-page visitors.'
        ''
        'First prompt: entrance access (browser HTTP Basic Auth)'
        'Username: demo-admin'
        "Password: $entrancePassword"
        ''
        'Second login: Delivo management application'
        'Username: admin'
        "Password: $appPassword"
        ''
        'These passwords apply to the separate delivo-demo database and entrance.'
        'Changing ADMIN_APP_PASSWORD after database initialization does not update the stored password.'
    )
    Write-DemoFile -Path (Join-Path $DemoStatePath 'credentials.txt') -Content (($credentials -join "`n") + "`n")
    Write-Host 'Generated .env.demo and .demo/credentials.txt; reused the two AI keys from .env.'
}

$demoEnvironment = Read-DemoEnv
Invoke-DemoCompose -ComposeArgs @('config', '--quiet')
Invoke-DemoCompose -ComposeArgs @('run', '--rm', '--no-deps', 'caddy', 'caddy', 'validate', '--config', '/etc/caddy/Caddyfile')
if (-not $SkipBuild) { Invoke-DemoCompose -ComposeArgs @('--parallel', '1', 'build') }
Invoke-DemoCompose -ComposeArgs @('up', '-d')

Write-Host 'Waiting for the backend (up to 5 minutes)...'
$ready = $false
$deadline = [DateTime]::UtcNow.AddMinutes(5)
while ([DateTime]::UtcNow -lt $deadline) {
    try {
        $status = Invoke-RestMethod -Uri 'http://127.0.0.1:18081/api/user/shop/status' -TimeoutSec 5
        if ($status.code -eq 1) { $ready = $true; break }
    } catch { }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'Backend is not ready. Check: docker compose --env-file .env.demo -f compose.demo.yml logs --tail 80 delivo-backend' }
Test-DemoEntrance -UserUrl 'http://127.0.0.1:18081' -AdminUrl 'http://127.0.0.1:18082' -Environment $demoEnvironment

# Open the fresh demonstration shop once; later starts preserve its saved status.
New-Item -ItemType Directory -Path $DemoStatePath -Force | Out-Null
$shopMarker = Join-Path $DemoStatePath 'shop-initialized'
if (-not (Test-Path -LiteralPath $shopMarker)) {
    $adminHeaders = Get-DemoAdminHeaders -Environment $demoEnvironment
    $loginBody = @{ username = 'admin'; password = $demoEnvironment['ADMIN_APP_PASSWORD'] } | ConvertTo-Json -Compress
    $login = Invoke-RestMethod -Uri 'http://127.0.0.1:18082/api/employee/login' -Method POST -Headers $adminHeaders -ContentType 'application/json' -Body $loginBody
    if ($login.code -ne 1) { throw 'Demo management application login failed.' }
    $adminHeaders['token'] = $login.data.token
    $opened = Invoke-RestMethod -Uri 'http://127.0.0.1:18082/api/shop/1' -Method PUT -Headers $adminHeaders
    if ($opened.code -ne 1) { throw 'Unable to open the demo shop.' }
    Write-DemoFile -Path $shopMarker -Content ([DateTime]::UtcNow.ToString('o'))
    Write-Host 'Opened the separate demo shop.'
}

# Publish only after the local entrance checks pass.
Invoke-DemoCompose -ComposeArgs @('--profile', 'tunnel', 'up', '-d', '--no-deps', 'user-tunnel', 'admin-tunnel')
New-Item -ItemType Directory -Path $DemoStatePath -Force | Out-Null
$urls = @{}
$deadline = [DateTime]::UtcNow.AddMinutes(2)
while ($urls.Count -lt 2 -and [DateTime]::UtcNow -lt $deadline) {
    foreach ($service in @('user-tunnel', 'admin-tunnel')) {
        if ($urls.ContainsKey($service)) { continue }
        $tunnelLog = & docker compose --env-file $DemoEnvPath -f $DemoComposePath logs --no-color --tail 100 $service 2>&1 | Out-String
        $urlMatches = [regex]::Matches($tunnelLog, 'https://[a-z0-9-]+\.trycloudflare\.com')
        if ($urlMatches.Count -gt 0) { $urls[$service] = $urlMatches[$urlMatches.Count - 1].Value }
    }
    if ($urls.Count -lt 2) { Start-Sleep -Seconds 2 }
}
if ($urls.Count -lt 2) { throw 'A tunnel URL was not issued. Check user-tunnel and admin-tunnel logs, then restart those services.' }
$result = [ordered]@{ user = $urls['user-tunnel']; admin = $urls['admin-tunnel']; active = $true; createdAt = [DateTime]::UtcNow.ToString('o') }
Write-DemoFile -Path (Join-Path $DemoStatePath 'urls.json') -Content (($result | ConvertTo-Json) + "`n")
Write-Host "User HTTPS URL: $($result.user)"
Write-Host "Admin HTTPS URL: $($result.admin)"
Write-Host "Admin credentials: $(Join-Path $DemoStatePath 'credentials.txt')"
Write-Host 'Keep this computer and Docker Desktop running. Stop with: ./deploy/stop-demo.ps1'
