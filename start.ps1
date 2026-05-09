# Ucitaj .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]*)=(.*)') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), "Process")
    }
}

# Ucitaj PEM fajlove kao env varijable
$env:JWT_PRIVATE_KEY = Get-Content $env:JWT_PRIVATE_KEY_PATH -Raw
$env:JWT_PUBLIC_KEY  = Get-Content $env:JWT_PUBLIC_KEY_PATH -Raw

# Pokreni servise (eureka prvi, pa ostali)
Start-Process powershell { cd eureka-server; mvn spring-boot:run }
Start-Sleep 10
Start-Process powershell { cd user-service; mvn spring-boot:run }
Start-Process powershell { cd api-gateway; mvn spring-boot:run }
Start-Process powershell { cd management-service; mvn spring-boot:run }
Start-Process powershell { cd report-service; mvn spring-boot:run }
Start-Process powershell { cd notification-service; mvn spring-boot:run }