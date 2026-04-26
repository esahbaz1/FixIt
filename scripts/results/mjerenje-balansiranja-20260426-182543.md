# Mjerenje Balansiranja

Date: 2026-04-26 18:25:44
Broj zahtjeva po nacinu: 100

## Bez balansiranja (DIREKTNO)
- Total service-side measured time: 1039 ms
- Average service-side measured time: 10.39 ms
- Total wall time: 2703 ms
- Average wall time: 27.03 ms
- Distribution:
- user-service:8081: 100

## Sa Eureka + Spring Cloud LoadBalancer
- Total service-side measured time: 603 ms
- Average service-side measured time: 6.03 ms
- Total wall time: 1034 ms
- Average wall time: 10.34 ms
- Distribution:
- user-service:8081: 50
- user-service:8085: 50
