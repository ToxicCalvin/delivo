param([switch]$Public, [switch]$SkipAi)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'demo-common.ps1')

function Invoke-DemoApi {
    param([string]$Url, [string]$Method = 'GET', [hashtable]$Headers = @{}, $Body = $null)
    $request = @{ Uri = $Url; Method = $Method; Headers = $Headers; TimeoutSec = 90 }
    if ($null -ne $Body) {
        $request.ContentType = 'application/json'
        $request.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 10 -Compress))
    }
    $response = Invoke-RestMethod @request
    if ($response.code -ne 1) { throw "API failed ($Method $Url): $($response.msg)" }
    return $response
}

$environment = Read-DemoEnv
$userUrl = 'http://127.0.0.1:18081'
$adminUrl = 'http://127.0.0.1:18082'
if ($Public) {
    $urls = Get-Content -LiteralPath (Join-Path $DemoStatePath 'urls.json') -Raw | ConvertFrom-Json
    $userUrl = $urls.user
    $adminUrl = $urls.admin
}
Test-DemoEntrance -UserUrl $userUrl -AdminUrl $adminUrl -Environment $environment

$adminHeaders = Get-DemoAdminHeaders -Environment $environment
$adminLogin = Invoke-DemoApi -Url "$adminUrl/api/employee/login" -Method POST -Headers $adminHeaders -Body @{ username = 'admin'; password = $environment['ADMIN_APP_PASSWORD'] }
$adminHeaders['token'] = $adminLogin.data.token
Write-Host 'PASS: Management application login'
$originalStatus = (Invoke-DemoApi -Url "$adminUrl/api/shop/status" -Headers $adminHeaders).data
$socket = $null
$cancellation = $null
try {
    if ($originalStatus -ne 1) { Invoke-DemoApi -Url "$adminUrl/api/shop/1" -Method PUT -Headers $adminHeaders | Out-Null }
    $suffix = [Guid]::NewGuid().ToString('N').Substring(0, 10)
    $username = "demo-check-$suffix"
    $password = New-DemoSecret -Bytes 12
    Invoke-DemoApi -Url "$userUrl/api/user/user/register" -Method POST -Body @{ username = $username; password = $password; name = 'Deployment check'; phone = '13800000000' } | Out-Null
    $userLogin = Invoke-DemoApi -Url "$userUrl/api/user/user/accountLogin" -Method POST -Body @{ username = $username; password = $password }
    $userHeaders = @{ authentication = $userLogin.data.token }
    Write-Host 'PASS: User registration and login'

    $categories = @( (Invoke-DemoApi -Url "$userUrl/api/user/category/list?type=1").data )
    $dish = $null
    foreach ($category in $categories) {
        $dishes = @( (Invoke-DemoApi -Url "$userUrl/api/user/dish/list?categoryId=$($category.id)").data )
        if ($dishes.Count -gt 0) { $dish = $dishes[0]; break }
    }
    if ($null -eq $dish) { throw 'No active dish found in the demo database.' }
    Write-Host 'PASS: Categories and menu data'

    Invoke-DemoApi -Url "$userUrl/api/user/addressBook" -Method POST -Headers $userHeaders -Body @{ consignee = 'Deployment check'; phone = '13800000000'; sex = '1'; provinceName = 'Demo'; cityName = 'Demo'; districtName = 'Demo'; detail = 'Demo address - no real delivery'; label = 'Demo' } | Out-Null
    $addresses = @( (Invoke-DemoApi -Url "$userUrl/api/user/addressBook/list" -Headers $userHeaders).data )
    if ($addresses.Count -eq 0) { throw 'Created address was not returned.' }
    Invoke-DemoApi -Url "$userUrl/api/user/addressBook/default" -Method PUT -Headers $userHeaders -Body @{ id = $addresses[0].id } | Out-Null
    Write-Host 'PASS: Create and select a delivery address'

    Invoke-DemoApi -Url "$userUrl/api/user/shoppingCart/add" -Method POST -Headers $userHeaders -Body @{ dishId = $dish.id; dishFlavor = '' } | Out-Null
    $cart = @( (Invoke-DemoApi -Url "$userUrl/api/user/shoppingCart/list" -Headers $userHeaders).data )
    if ($cart.Count -ne 1) { throw 'Cart did not contain the added dish.' }
    Write-Host 'PASS: Add dish and read cart'

    $socket = [Net.WebSockets.ClientWebSocket]::new()
    $socket.Options.SetRequestHeader('Authorization', $adminHeaders['Authorization'])
    $socketUrl = $adminUrl -replace '^https:', 'wss:' -replace '^http:', 'ws:'
    $cancellation = [Threading.CancellationTokenSource]::new()
    $cancellation.CancelAfter(30000)
    $socket.ConnectAsync([Uri]"$socketUrl/ws/deploy-check-$suffix", $cancellation.Token).GetAwaiter().GetResult() | Out-Null
    if ($socket.State -ne [Net.WebSockets.WebSocketState]::Open) { throw 'Admin WebSocket did not open.' }
    Write-Host 'PASS: Protected management WebSocket connects'
    $buffer = New-Object byte[] 4096
    $notificationTask = $socket.ReceiveAsync([ArraySegment[byte]]::new($buffer), $cancellation.Token)

    $order = (Invoke-DemoApi -Url "$userUrl/api/user/order/submit" -Method POST -Headers $userHeaders -Body @{ addressBookId = $addresses[0].id; payMethod = 1; remark = 'Deployment check - simulated order'; deliveryStatus = 1; tablewareNumber = 1; tablewareStatus = 1; packAmount = 0; amount = [decimal]$dish.price }).data
    Invoke-DemoApi -Url "$userUrl/api/user/order/mockPayment" -Method PUT -Headers $userHeaders -Body @{ orderNumber = $order.orderNumber; payMethod = 1 } | Out-Null
    $notification = $notificationTask.GetAwaiter().GetResult()
    $notificationText = [Text.Encoding]::UTF8.GetString($buffer, 0, $notification.Count)
    if (($notificationText | ConvertFrom-Json).type -ne 1) { throw 'Management WebSocket did not receive the new order notification.' }
    Write-Host 'PASS: Order, mock payment and real-time management notification'

    $detail = (Invoke-DemoApi -Url "$userUrl/api/user/order/orderDetail/$($order.id)" -Headers $userHeaders).data
    if ($detail.status -ne 2) { throw "Paid demo order has unexpected status: $($detail.status)" }
    $managementDetail = (Invoke-DemoApi -Url "$adminUrl/api/order/details/$($order.id)" -Headers $adminHeaders).data
    if ($managementDetail.number -ne $order.orderNumber) { throw 'Management order detail does not match the created order.' }
    Write-Host 'PASS: User and management order details agree'

    if (-not $SkipAi) {
        $aiResponse = (Invoke-DemoApi -Url "$userUrl/api/user/ai/chat" -Method POST -Headers $userHeaders -Body 'Recommend a vegetarian dish from the menu. Do not add anything to my cart or place an order.').data
        $envelope = $aiResponse | ConvertFrom-Json
        if ([string]::IsNullOrWhiteSpace($envelope.text) -or @($envelope.cards).Count -lt 1) { throw 'AI assistant did not return readable text and recommended dish cards.' }
        Write-Host 'PASS: AI assistant returns readable text and recommended dish cards'
    }
    New-Item -ItemType Directory -Path $DemoStatePath -Force | Out-Null
    $summary = [ordered]@{ checkedAt = [DateTime]::UtcNow.ToString('o'); public = [bool]$Public; userUrl = $userUrl; adminUrl = $adminUrl; orderId = $order.id; orderNumber = $order.orderNumber; aiChecked = -not [bool]$SkipAi }
    Write-DemoFile -Path (Join-Path $DemoStatePath 'last-check.json') -Content (($summary | ConvertTo-Json) + "`n")
    Write-Host "All requested checks passed. Simulated order: $($order.orderNumber)"
} finally {
    if ($null -ne $socket) {
        if ($socket.State -eq [Net.WebSockets.WebSocketState]::Open) {
            $closeTimeout = [Threading.CancellationTokenSource]::new()
            $closeTimeout.CancelAfter(5000)
            try { $socket.CloseAsync([Net.WebSockets.WebSocketCloseStatus]::NormalClosure, 'Deployment check finished', $closeTimeout.Token).GetAwaiter().GetResult() | Out-Null }
            catch { $socket.Abort() }
            finally { $closeTimeout.Dispose() }
        }
        $socket.Dispose()
    }
    if ($null -ne $cancellation) { $cancellation.Dispose() }
    if ($originalStatus -ne 1) { Invoke-DemoApi -Url "$adminUrl/api/shop/$originalStatus" -Method PUT -Headers $adminHeaders | Out-Null }
}
