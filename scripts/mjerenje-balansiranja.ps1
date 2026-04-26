param(
    [string]$ManagementServiceBaseUrl = "http://localhost:8082",
    [int]$Requests = 100
)

$ErrorActionPreference = "Stop"

function Invoke-Benchmark {
    param(
        [string]$Name,
        [string]$Path,
        [int]$TotalRequests
    )

    $distribution = @{}
    $serviceTimeMs = 0L
    $wallWatch = [System.Diagnostics.Stopwatch]::StartNew()

    for ($i = 1; $i -le $TotalRequests; $i++) {
        $response = Invoke-RestMethod -Uri "$ManagementServiceBaseUrl$Path" -Method Get -TimeoutSec 10
        $instance = [string]$response.userServiceInstance.instanceId

        if (-not $distribution.ContainsKey($instance)) {
            $distribution[$instance] = 0
        }
        $distribution[$instance]++
        $serviceTimeMs += [int64]$response.durationMs
    }

    $wallWatch.Stop()

    [pscustomobject]@{
        Mode = $Name
        Requests = $TotalRequests
        TotalServiceTimeMs = $serviceTimeMs
        AvgServiceTimeMs = [math]::Round($serviceTimeMs / $TotalRequests, 2)
        TotalWallTimeMs = $wallWatch.ElapsedMilliseconds
        AvgWallTimeMs = [math]::Round($wallWatch.ElapsedMilliseconds / $TotalRequests, 2)
        Distribution = $distribution
    }
}

function Write-Distribution {
    param(
        [hashtable]$Distribution
    )

    foreach ($key in ($Distribution.Keys | Sort-Object)) {
        Write-Host ("  {0} -> {1}" -f $key, $Distribution[$key])
    }
}

Write-Host "Pokrecem mjerenje BEZ balansiranja opterecenja..."
$direct = Invoke-Benchmark -Name "DIREKTNO" -Path "/api/otkrivanje/korisnik-instanca/direktno" -TotalRequests $Requests

Write-Host "Pokrecem mjerenje SA balansiranjem (Eureka + Spring Cloud LoadBalancer)..."
$lb = Invoke-Benchmark -Name "BALANSIRANO" -Path "/api/otkrivanje/korisnik-instanca/balansirano" -TotalRequests $Requests

Write-Host ""
Write-Host "Rezultati:"
Write-Host ("DIREKTNO     -> totalService={0}ms avgService={1}ms totalWall={2}ms avgWall={3}ms" -f `
    $direct.TotalServiceTimeMs, $direct.AvgServiceTimeMs, $direct.TotalWallTimeMs, $direct.AvgWallTimeMs)
Write-Host ("BALANSIRANO  -> totalService={0}ms avgService={1}ms totalWall={2}ms avgWall={3}ms" -f `
    $lb.TotalServiceTimeMs, $lb.AvgServiceTimeMs, $lb.TotalWallTimeMs, $lb.AvgWallTimeMs)

Write-Host ""
Write-Host "DIREKTNO raspodjela:"
Write-Distribution -Distribution $direct.Distribution
Write-Host "BALANSIRANO raspodjela:"
Write-Distribution -Distribution $lb.Distribution

$reportDir = Join-Path $PSScriptRoot "results"
if (-not (Test-Path $reportDir)) {
    New-Item -Path $reportDir -ItemType Directory | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = Join-Path $reportDir "mjerenje-balansiranja-$timestamp.md"

$directRows = ($direct.Distribution.GetEnumerator() | Sort-Object Name | ForEach-Object { "- $($_.Name): $($_.Value)" }) -join "`n"
$lbRows = ($lb.Distribution.GetEnumerator() | Sort-Object Name | ForEach-Object { "- $($_.Name): $($_.Value)" }) -join "`n"

$report = @"
# Mjerenje Balansiranja

Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Broj zahtjeva po nacinu: $Requests

## Bez balansiranja (DIREKTNO)
- Total service-side measured time: $($direct.TotalServiceTimeMs) ms
- Average service-side measured time: $($direct.AvgServiceTimeMs) ms
- Total wall time: $($direct.TotalWallTimeMs) ms
- Average wall time: $($direct.AvgWallTimeMs) ms
- Distribution:
$directRows

## Sa Eureka + Spring Cloud LoadBalancer
- Total service-side measured time: $($lb.TotalServiceTimeMs) ms
- Average service-side measured time: $($lb.AvgServiceTimeMs) ms
- Total wall time: $($lb.TotalWallTimeMs) ms
- Average wall time: $($lb.AvgWallTimeMs) ms
- Distribution:
$lbRows
"@

Set-Content -Path $reportFile -Value $report
Write-Host ""
Write-Host "Izvjestaj sacuvan u: $reportFile"
