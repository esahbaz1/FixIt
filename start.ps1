$root = $PSScriptRoot

# =========================
# ENV VARIJABLE (centralno)
# =========================
$env:JWT_PRIVATE_KEY="MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCVcVJDGermNuaXOuOaNDocDFx8AVAGCh6MLhyXudEbaAWMDV4NPeQz/Ayx3eQByNCHm50t6Y4on3nsOec2MNk4tqWD6xB1Iq/ZAonjMJ5LU17NLuyUyXItpbv7WFmmdVCDCnHD1bqKXjLbD3xEDOL8uxrKlTrLGUDA0ypgCYicB6d6LCu87G6W5UOEF01gDU7PgRzC22rbLllr1unsBfFNFpzVs9FaeghIptcO0OHiY61COCLK8Yuqs79C8bKkY7wjoFQ8DJFA8W+G2ga6CcY6chYQZ3/ASBl23eD5P1NDX3HenoaYxmQaLwIHcR+1wgJbOkjSnnwUZ/UrrUL/unlpAgMBAAECggEADhmgmNsvqiQA8sDoK6sE1pOmAD9BdShsRG6vXPN30t2oNFQojRhLWUpNSlT0x+RSJJFera/NLXEKmGrIMXk/bZXyHPXHWj35GIEgUHLMZLALkFF+mfU91CfAilEGOGn2PD72h/M5BAgqRTf7UhJjMlRXDUkyQGp0DR1/cItFu+7qCP5VjOwz5ewc6/NUpzlwb3ICr4klQwKo1VY6J3L76vIvs7ChJRhqL/KXE+fE/h0dls7rqJc3OuujHYeEdhw0sALGoC/IpU9QWm4glxKbpfqdk2C5uypv4w7HQo4tCbw7b207hlud6A+erk2/pSnIDBqOzZEBLfZK6cF7rJm3QQKBgQDGbeakf/rOGbbI3si+4YdHclOJ9UQ1dw//7yfcdtd+thkzwA/jvz+q9tCiWrZPuXdnxlNjRpAUOHcIAu2Z/kyWtoiWMtBq9/QVA+ktqmu6H/laK6i8Pt4ZzfnNXpMsrpt2aX/0TNO2pYtgHcXBX+g7FjAcgnAMLDKSlCpD6PAqWQKBgQDAzP8QE6g15fHv85BmWqq5REciAWkOLajR69d1t7TRhigBZu/s9JFbIZKuPCGnluYdc+xt5UNa6mhZuCzq5V05lEejFf10lx00Wc9qiLnxKzUKuGnYxOkd4d8y6OwMa2/JiujrHiHnkxaSKb1Wo3V/ZzYk8FOVernfNJGdHHzFkQKBgC4NT5wZ/7FZkmxoGBsE6IqSZAT+oUfvTA1QCeZGxpin1o2GI6nttu8MvTsQ5oL2PlodUzkVJVcsC6QWWeKFfGz2DBkfzfMlfMZr0/A/PyVBSnO88jpONOm0PieFrY6PL5F5xrKCKhiXrxMtlbndcl5UNfJH641HrK0MKgr8wtBxAoGAGYwQUCUKqPmrMUxo2ecxsBLoCg19yi8qq4ZAqoyJHSpVqrnlGNTfXd/4+7VkEDziiQyPS5CReD6PTTQmX4m87KBFTcrgJs7PQ9ySq18qpFZBVQZSoDKXYSpI1QogHRUvXtlVO7Jmc7T9zKhTOmO2cgdPGV3u9WOptXXVxae8sMECgYBybpJ6BnRt/R4G6Mq7/ffQYJgXdY2HI1WVPw6IZO5nY8NDOemvsKF+sJ6PIcMCjYdypunrG48rT6BEGRf75RUGb9cC5jB+uLtGga7PSHcGmV21Gdu8nOEY2/b29T+K58oHzcKS01745DZsx6segvJYZtdRolkGist+A4KwlmBb4w=="
$env:JWT_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlXFSQxnq5jbmlzrjmjQ6HAxcfAFQBgoejC4cl7nRG2gFjA1eDT3kM/wMsd3kAcjQh5udLemOKJ957DnnNjDZOLalg+sQdSKv2QKJ4zCeS1NezS7slMlyLaW7+1hZpnVQgwpxw9W6il4y2w98RAzi/LsaypU6yxlAwNMqYAmInAeneiwrvOxuluVDhBdNYA1Oz4Ecwttq2y5Za9bp7AXxTRac1bPRWnoISKbXDtDh4mOtQjgiyvGLqrO/QvGypGO8I6BUPAyRQPFvhtoGugnGOnIWEGd/wEgZdt3g+T9TQ19x3p6GmMZkGi8CB3EftcICWzpI0p58FGf1K61C/7p5aQIDAQAB"
$env:GATEWAY_SECRET="KoF6fYufioYCIzf8mntaTUYY5LsOTmODoSfd3EUgCCg"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="password"

Write-Host "Env varijable postavljene!" -ForegroundColor Green


# =========================
# FUNKCIJA ZA SERVISE
# =========================
function Start-Servis($dir) {
    $path = Join-Path $root $dir

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$path'; mvn spring-boot:run"
    ) -WindowStyle Normal

    Write-Host "Pokrenut: $dir" -ForegroundColor Green
}

# =========================
# FUNKCIJA ZA FRONTEND
# =========================
function Start-Frontend($dir) {
    $path = Join-Path $root $dir

    Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "cd '$path'; npm install; npm run dev"
    ) -WindowStyle Normal

    Write-Host "Pokrenut frontend: $dir" -ForegroundColor Green
}

# =========================
# START
# =========================
Write-Host "Pokretanje FixIt servisa..." -ForegroundColor Cyan

Start-Servis "eureka-server"
Write-Host "Cekam da se Eureka pokrene (15s)..." -ForegroundColor Yellow
Start-Sleep 15

Start-Servis "user-service"
Start-Servis "api-gateway"
Start-Servis "management-service"
Start-Servis "report-service"
Start-Servis "notification-service"

# =========================
# FRONTEND
# =========================
Write-Host "Pokrecem frontend..." -ForegroundColor Cyan
Start-Frontend "frontend"

Write-Host "Svi servisi i frontend su pokrenuti." -ForegroundColor Cyan
Write-Host "Eureka dashboard: http://localhost:8761"