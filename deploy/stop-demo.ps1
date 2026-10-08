$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'demo-common.ps1')
Invoke-DemoCompose -ComposeArgs @('--profile', 'tunnel', 'down')
$urlPath = Join-Path $DemoStatePath 'urls.json'
if (Test-Path -LiteralPath $urlPath) {
    $urls = Get-Content -LiteralPath $urlPath -Raw | ConvertFrom-Json
    $urls | Add-Member -NotePropertyName active -NotePropertyValue $false -Force
    $urls | Add-Member -NotePropertyName stoppedAt -NotePropertyValue ([DateTime]::UtcNow.ToString('o')) -Force
    Write-DemoFile -Path $urlPath -Content (($urls | ConvertTo-Json) + "`n")
}
Write-Host 'Stopped the demo and both public tunnels. Database, Redis and AI cache volumes are preserved.'
