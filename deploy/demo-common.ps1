Set-StrictMode -Version Latest

$DemoRoot = Split-Path -Parent $PSScriptRoot
$DemoEnvPath = Join-Path $DemoRoot '.env.demo'
$DemoStatePath = Join-Path $DemoRoot '.demo'
$DemoComposePath = Join-Path $DemoRoot 'compose.demo.yml'

function Read-DemoEnv {
    param([string]$Path = $DemoEnvPath)
    if (-not (Test-Path -LiteralPath $Path)) { throw "Missing environment file: $Path" }
    $values = @{}
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        if ($line -match '^\s*([A-Z][A-Z0-9_]*)=(.*)$') {
            $value = $Matches[2].Trim()
            if ($value.Length -ge 2 -and (($value.StartsWith("'") -and $value.EndsWith("'")) -or ($value.StartsWith('"') -and $value.EndsWith('"')))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            $values[$Matches[1]] = $value
        }
    }
    return $values
}

function Write-DemoFile {
    param([string]$Path, [string]$Content)
    [IO.File]::WriteAllText($Path, $Content.Replace("`r`n", "`n"), [Text.UTF8Encoding]::new($false))
}

function Invoke-DemoCompose {
    param([string[]]$ComposeArgs)
    & docker compose --env-file $DemoEnvPath -f $DemoComposePath @ComposeArgs
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose failed: $($ComposeArgs -join ' ')" }
}

function New-DemoSecret {
    param([int]$Bytes = 24)
    $buffer = New-Object byte[] $Bytes
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $random.GetBytes($buffer) } finally { $random.Dispose() }
    return ([BitConverter]::ToString($buffer)).Replace('-', '').ToLowerInvariant()
}

function Get-DemoHttpStatus {
    param([string]$Url, [hashtable]$Headers = @{}, [string]$Method = 'GET')
    try {
        $response = Invoke-WebRequest -Uri $Url -Headers $Headers -Method $Method -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
        return [int]$response.StatusCode
    } catch {
        if ($null -ne $_.Exception.Response) { return [int]$_.Exception.Response.StatusCode }
        throw
    }
}

function Get-DemoAdminHeaders {
    param([hashtable]$Environment)
    $pair = '{0}:{1}' -f $Environment['ADMIN_BASIC_AUTH_USER'], $Environment['ADMIN_BASIC_AUTH_PASSWORD']
    return @{ Authorization = 'Basic ' + [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($pair)) }
}

function Test-DemoEntrance {
    param([string]$UserUrl, [string]$AdminUrl, [hashtable]$Environment)
    $checks = @(
        @{ Url = "$UserUrl/"; Status = 200; Name = 'User page' },
        @{ Url = "$UserUrl/api/employee/login"; Status = 404; Name = 'Admin API blocked on user entrance' },
        @{ Url = "$UserUrl/api/user/order/payment"; Status = 404; Name = 'Real payment blocked' },
        @{ Url = "$UserUrl/api/user/order/payment/"; Status = 404; Name = 'Real payment trailing slash blocked' },
        @{ Url = "$UserUrl/ws/demo-check"; Status = 404; Name = 'Public WebSocket blocked' },
        @{ Url = "$AdminUrl/"; Status = 401; Name = 'Admin page requires entrance password' },
        @{ Url = "$AdminUrl/api/employee/login"; Status = 401; Name = 'Admin API requires entrance password' },
        @{ Url = "$AdminUrl/ws/demo-check"; Status = 401; Name = 'Admin WebSocket requires entrance password' }
    )
    foreach ($check in $checks) {
        $actual = Get-DemoHttpStatus -Url $check.Url
        if ($actual -ne $check.Status) { throw "$($check.Name): expected $($check.Status), got $actual" }
        Write-Host "PASS: $($check.Name)"
    }
    $actual = Get-DemoHttpStatus -Url "$AdminUrl/" -Headers (Get-DemoAdminHeaders -Environment $Environment)
    if ($actual -ne 200) { throw "Admin entrance password check failed: HTTP $actual" }
    Write-Host 'PASS: Admin page opens with the entrance password'
}
