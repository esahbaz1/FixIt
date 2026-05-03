param(
    [string]$GatewayBaseUrl           = "http://localhost:8080",
    [string]$ManagementServiceBaseUrl = "http://localhost:8082",
    [int]$Requests = 100
)

$ErrorActionPreference = "Stop"

# ─── JWT token ────────────────────────────────────────────────────────────────
# Postavi varijablu okruzenja FIXIT_JWT ili ce skripta pokusati auto-prijavu.

$JwtToken = $env:FIXIT_JWT

if (-not $JwtToken) {
    Write-Host "INFO: FIXIT_JWT nije postavljen, pokusavam automatsku prijavu..." -ForegroundColor Yellow
    try {
        $loginBody = @{ email = "gradjanin@test.ba"; lozinka = "test1234" } | ConvertTo-Json
        $loginResponse = Invoke-RestMethod `
            -Uri "$GatewayBaseUrl/api/auth/prijava" `
            -Method Post `
            -ContentType "application/json" `
            -Body $loginBody
        $JwtToken = $loginResponse.token
        Write-Host "  Prijavljen kao gradjanin@test.ba" -ForegroundColor Green
    } catch {
        Write-Host "  Automatska prijava nije uspjela: $_" -ForegroundColor Red
        Write-Host "  Postavi: `$env:FIXIT_JWT = '<tvoj_token>'" -ForegroundColor Yellow
        exit 1
    }
}

$AuthHeader = @{ Authorization = "Bearer $JwtToken" }

# ─── Pomocne funkcije ─────────────────────────────────────────────────────────

function Invoke-BenchmarkDirect {
    param(
        [string]$Name,
        [string]$Url,
        [int]$TotalRequests,
        [hashtable]$Headers = @{}
    )
    $distribution = @{}
    $serviceTimeMs = 0L
    $errors = 0
    $wallWatch = [System.Diagnostics.Stopwatch]::StartNew()

    for ($i = 1; $i -le $TotalRequests; $i++) {
        try {
            $response = Invoke-RestMethod -Uri $Url -Method Get -Headers $Headers -TimeoutSec 10
            $instance = [string]$response.userServiceInstance.instanceId
            if (-not $distribution.ContainsKey($instance)) { $distribution[$instance] = 0 }
            $distribution[$instance]++
            $serviceTimeMs += [int64]$response.durationMs
        } catch { $errors++ }
    }

    $wallWatch.Stop()
    $n = $TotalRequests - $errors
    [pscustomobject]@{
        Mode           = $Name
        Requests       = $TotalRequests
        Errors         = $errors
        TotalServiceMs = $serviceTimeMs
        AvgServiceMs   = if ($n -gt 0) { [math]::Round($serviceTimeMs / $n, 2) } else { 0 }
        TotalWallMs    = $wallWatch.ElapsedMilliseconds
        AvgWallMs      = [math]::Round($wallWatch.ElapsedMilliseconds / $TotalRequests, 2)
        Distribution   = $distribution
    }
}

function Invoke-BenchmarkSinhrono {
    # Mjeri sinhronu komunikaciju report-service -> user-service.
    # Svaki POST /api/prijave uzrokuje UserServiceKlijent.validirajKorisnika()
    # koji poziva GET /api/korisnici/{id} kroz @LoadBalanced RestTemplate.
    param(
        [string]$Name,
        [int]$TotalRequests,
        [hashtable]$Headers
    )

    $distribution = @{}
    $latencies = [System.Collections.Generic.List[long]]::new()
    $errors = 0
    $wallWatch = [System.Diagnostics.Stopwatch]::StartNew()

    # Dohvati prvu dostupnu kategoriju
    $kategorijaId = 1
    try {
        $kategorije = Invoke-RestMethod `
            -Uri "$GatewayBaseUrl/api/prijave/kategorije" `
            -Method Get -Headers $Headers -TimeoutSec 10
        if ($kategorije.Count -gt 0) { $kategorijaId = $kategorije[0].id }
    } catch { }

    for ($i = 1; $i -le $TotalRequests; $i++) {
        $reqWatch = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            $tijelo = @{
    naslov       = "Benchmark prijava $i"
    opis         = "Automatski generisana prijava za mjerenje balansiranja"
    latitude     = 43.8563
    longitude    = 18.4131
    kategorijaId = $kategorijaId
    korisnikId   = 13
} | ConvertTo-Json

            $response = Invoke-RestMethod `
                -Uri "$GatewayBaseUrl/api/prijave" `
                -Method Post `
                -Headers ($Headers + @{ "Content-Type" = "application/json" }) `
                -Body $tijelo `
                -TimeoutSec 10

            $reqWatch.Stop()
            $latencies.Add($reqWatch.ElapsedMilliseconds)

            # Ako PrijavaResponseDTO ima userServiceInstanceId — prikazujemo raspodjelu.
            # Ako nema, biljeximo samo "user-service" kao jednu grupu.
            $instance = if ($response.PSObject.Properties.Name -contains "userServiceInstanceId" `
                            -and $response.userServiceInstanceId) {
                [string]$response.userServiceInstanceId
            } else {
                "user-service"
            }
            if (-not $distribution.ContainsKey($instance)) { $distribution[$instance] = 0 }
            $distribution[$instance]++
        } catch {
            $reqWatch.Stop()
            $errors++
        }
    }

    $wallWatch.Stop()
    $total = if ($latencies.Count -gt 0) { ($latencies | Measure-Object -Sum).Sum } else { 0 }

    [pscustomobject]@{
        Mode           = $Name
        Requests       = $TotalRequests
        Errors         = $errors
        TotalServiceMs = $total
        AvgServiceMs   = if ($latencies.Count -gt 0) { [math]::Round($total / $latencies.Count, 2) } else { 0 }
        MinMs          = if ($latencies.Count -gt 0) { ($latencies | Measure-Object -Minimum).Minimum } else { 0 }
        MaxMs          = if ($latencies.Count -gt 0) { ($latencies | Measure-Object -Maximum).Maximum } else { 0 }
        TotalWallMs    = $wallWatch.ElapsedMilliseconds
        AvgWallMs      = [math]::Round($wallWatch.ElapsedMilliseconds / $TotalRequests, 2)
        Distribution   = $distribution
    }
}

function Write-Distribution {
    param([hashtable]$Distribution)
    foreach ($key in ($Distribution.Keys | Sort-Object)) {
        $count = $Distribution[$key]
        $bar = "#" * [math]::Min([math]::Round($count / 2), 50)
        Write-Host ("  {0,-40} -> {1,4}  {2}" -f $key, $count, $bar)
    }
}

function Format-ResultLine {
    param([pscustomobject]$r)
    $errStr = if ($r.Errors -gt 0) { "  !! Greske: $($r.Errors)" } else { "" }
    "totalService={0}ms  avgService={1}ms  totalWall={2}ms  avgWall={3}ms{4}" -f `
        $r.TotalServiceMs, $r.AvgServiceMs, $r.TotalWallMs, $r.AvgWallMs, $errStr
}

function ConvertTo-DistributionMd {
    param([hashtable]$Distribution)
    if ($Distribution.Count -eq 0) { return "  (nema podataka)" }
    ($Distribution.GetEnumerator() | Sort-Object Name | ForEach-Object { "- $($_.Name): $($_.Value)" }) -join "`n"
}

# ─── DIO 1: management-service -> user-service ───────────────────────────────

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host " DIO 1: management-service -> user-service (direktno vs lb)     " -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Pokrecem mjerenje BEZ balansiranja (direktno na port 8081)..."
$direct = Invoke-BenchmarkDirect `
    -Name "DIREKTNO" `
    -Url "$GatewayBaseUrl/api/otkrivanje/korisnik-instanca/direktno" `
    -TotalRequests $Requests `
    -Headers $AuthHeader

Write-Host "Pokrecem mjerenje SA balansiranjem (Eureka + Spring Cloud LoadBalancer)..."
$lb = Invoke-BenchmarkDirect `
    -Name "BALANSIRANO" `
    -Url "$GatewayBaseUrl/api/otkrivanje/korisnik-instanca/balansirano" `
    -TotalRequests $Requests `
    -Headers $AuthHeader

Write-Host ""
Write-Host "Rezultati (management-service -> user-service):"
Write-Host ("  DIREKTNO    -> " + (Format-ResultLine $direct))
Write-Host ("  BALANSIRANO -> " + (Format-ResultLine $lb))
Write-Host ""
Write-Host "Raspodjela DIREKTNO:"
Write-Distribution -Distribution $direct.Distribution
Write-Host "Raspodjela BALANSIRANO:"
Write-Distribution -Distribution $lb.Distribution

# ─── DIO 2: report-service -> user-service (sinhrona komunikacija) ────────────

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host " DIO 2: report-service -> user-service (sinhrona komunikacija)   " -ForegroundColor Cyan
Write-Host " POST /api/prijave => UserServiceKlijent.validirajKorisnika()     " -ForegroundColor DarkGray
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pokrecem mjerenje sinhrone komunikacije report-service -> user-service..."
Write-Host "($Requests zahtjeva, svaki kreira prijavu i validira korisnika ka user-service)"
Write-Host ""

$sinhrono = Invoke-BenchmarkSinhrono `
    -Name "SINHRONO" `
    -TotalRequests $Requests `
    -Headers $AuthHeader

Write-Host "Rezultati (report-service -> user-service sinhrono):"
Write-Host ("  SINHRONO -> " + (Format-ResultLine $sinhrono))
Write-Host ("  min=" + $sinhrono.MinMs + "ms  max=" + $sinhrono.MaxMs + "ms")
Write-Host ""
Write-Host "Raspodjela user-service instanci (vidljiva samo ako imate vise instanci):"
Write-Distribution -Distribution $sinhrono.Distribution

if ($sinhrono.Distribution.Count -eq 1 -and $sinhrono.Distribution.ContainsKey("user-service")) {
    Write-Host ""
    Write-Host "  NAPOMENA: PrijavaResponseDTO ne sadrzi userServiceInstanceId." -ForegroundColor Yellow
    Write-Host "  Raspodjela je vidljiva u logovima report-service:" -ForegroundColor Yellow
    Write-Host "  'Sinhroni poziv user-service: GET http://user-service/api/korisnici/{id}'" -ForegroundColor DarkGray
    Write-Host "  Za tacnu raspodjelu po instancama dodaj userServiceInstanceId u odgovor." -ForegroundColor Yellow
}

# ─── Izvjestaj ────────────────────────────────────────────────────────────────

$reportDir = Join-Path $PSScriptRoot "results"
if (-not (Test-Path $reportDir)) { New-Item -Path $reportDir -ItemType Directory | Out-Null }

$timestamp  = Get-Date -Format "yyyyMMdd-HHmmss"
$reportFile = Join-Path $reportDir "mjerenje-balansiranja-$timestamp.md"

$report = @"
# Mjerenje Balansiranja

Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Broj zahtjeva po scenariju: $Requests

---

## DIO 1 — management-service → user-service

### Direktno (port 8081, bez Eureka)
- Total service time: $($direct.TotalServiceMs) ms
- Avg service time:   $($direct.AvgServiceMs) ms
- Total wall time:    $($direct.TotalWallMs) ms
- Avg wall time:      $($direct.AvgWallMs) ms
- Greske: $($direct.Errors)
- Raspodjela:
$(ConvertTo-DistributionMd $direct.Distribution)

### Balansirano (Eureka + Spring Cloud LoadBalancer)
- Total service time: $($lb.TotalServiceMs) ms
- Avg service time:   $($lb.AvgServiceMs) ms
- Total wall time:    $($lb.TotalWallMs) ms
- Avg wall time:      $($lb.AvgWallMs) ms
- Greske: $($lb.Errors)
- Raspodjela:
$(ConvertTo-DistributionMd $lb.Distribution)

---

## DIO 2 — report-service → user-service (sinhrona komunikacija)

Scenario: svaki POST /api/prijave uzrokuje sinhroni poziv
UserServiceKlijent.validirajKorisnika() -> GET /api/korisnici/{id}
RestTemplate u report-service je @LoadBalanced (Eureka balansira).

- Total latency (end-to-end kroz gateway): $($sinhrono.TotalServiceMs) ms
- Avg latency:  $($sinhrono.AvgServiceMs) ms
- Min latency:  $($sinhrono.MinMs) ms
- Max latency:  $($sinhrono.MaxMs) ms
- Total wall:   $($sinhrono.TotalWallMs) ms
- Avg wall:     $($sinhrono.AvgWallMs) ms
- Greske: $($sinhrono.Errors)
- Raspodjela user-service instanci:
$(ConvertTo-DistributionMd $sinhrono.Distribution)
"@

Set-Content -Path $reportFile -Value $report -Encoding UTF8
Write-Host ""
Write-Host "Izvjestaj sacuvan: $reportFile" -ForegroundColor Green
