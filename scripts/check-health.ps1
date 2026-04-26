param(
    [string]$EurekaBaseUrl = "http://localhost:8761",
    [string]$UserServiceBaseUrl = "http://localhost:8081",
    [string]$UserService2BaseUrl = "http://localhost:8085",
    [string]$ManagementServiceBaseUrl = "http://localhost:8082",
    [string]$ReportServiceBaseUrl = "http://localhost:8083",
    [string]$NotificationServiceBaseUrl = "http://localhost:8084"
)

$ErrorActionPreference = "Stop"

function Test-HealthEndpoint {
    param(
        [string]$Name,
        [string]$Url
    )

    try {
        $response = Invoke-RestMethod -Uri "$Url/actuator/health" -Method Get -TimeoutSec 10
        [pscustomobject]@{
            Name = $Name
            Url = "$Url/actuator/health"
            Status = $response.status
            Ok = ($response.status -eq "UP")
        }
    }
    catch {
        [pscustomobject]@{
            Name = $Name
            Url = "$Url/actuator/health"
            Status = "UNREACHABLE"
            Ok = $false
        }
    }
}

$results = @()
$results += Test-HealthEndpoint -Name "Eureka Server" -Url $EurekaBaseUrl
$results += Test-HealthEndpoint -Name "User Service (instance 1)" -Url $UserServiceBaseUrl
$results += Test-HealthEndpoint -Name "User Service (instance 2)" -Url $UserService2BaseUrl
$results += Test-HealthEndpoint -Name "Management Service" -Url $ManagementServiceBaseUrl
$results += Test-HealthEndpoint -Name "Report Service" -Url $ReportServiceBaseUrl
$results += Test-HealthEndpoint -Name "Notification Service" -Url $NotificationServiceBaseUrl

$results | Format-Table -AutoSize

if (($results | Where-Object { -not $_.Ok }).Count -gt 0) {
    exit 1
}
