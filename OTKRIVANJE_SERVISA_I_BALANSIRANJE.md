# Otkrivanje Servisa i Balansiranje

This project now includes:

1. Eureka service discovery server (`eureka-server`)
2. Eureka clients on all microservices:
   - `user-service`
   - `management-service`
   - `report-service`
   - `notification-service`
3. Health-check registration via actuator (`/actuator/health`)
4. Client-side load balancing test path in `management-service`

Note: Ribbon is deprecated in modern Spring Cloud.  
The implementation uses **Spring Cloud LoadBalancer**, which is the supported Ribbon replacement with Eureka.

## Start order

1. Start Eureka:

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\eureka-server
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

2. Start two `user-service` instances (local profile with H2):

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\user-service
$env:SPRING_PROFILES_ACTIVE="local"
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\user-service
$env:SPRING_PROFILES_ACTIVE="local,instance2"
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

3. Start `management-service` (used for benchmark requests):

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\management-service
$env:SPRING_PROFILES_ACTIVE="local"
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

Optional: start `report-service` and `notification-service` with local profile to verify discovery for all services:

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\report-service
$env:SPRING_PROFILES_ACTIVE="local"
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt\notification-service
$env:SPRING_PROFILES_ACTIVE="local"
C:\mvnd\mvn\bin\mvn.cmd spring-boot:run
```

## Health check verification

Run:

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt
powershell -ExecutionPolicy Bypass -File .\scripts\check-health.ps1
```

## Mjerenje sa 100 zahtjeva (bez i sa balansiranjem)

Run:

```powershell
cd C:\Users\DELL\Desktop\nwt\FixIt
powershell -ExecutionPolicy Bypass -File .\scripts\mjerenje-balansiranja.ps1 -Requests 100
```

The script prints:

- request processing time without LB
- request processing time with LB
- per-instance distribution for 100 requests in both modes

It also writes a report file under:

`scripts/results/mjerenje-balansiranja-<timestamp>.md`
