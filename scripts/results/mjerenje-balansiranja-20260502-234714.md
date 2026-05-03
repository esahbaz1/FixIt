# Mjerenje Balansiranja

Date: 2026-05-02 23:47:14
Broj zahtjeva po scenariju: 100

---

## DIO 1 â€” management-service â†’ user-service

### Direktno (port 8081, bez Eureka)
- Total service time: 623 ms
- Avg service time:   6.23 ms
- Total wall time:    2594 ms
- Avg wall time:      25.94 ms
- Greske: 0
- Raspodjela:
- user-service:8081: 100

### Balansirano (Eureka + Spring Cloud LoadBalancer)
- Total service time: 687 ms
- Avg service time:   6.87 ms
- Total wall time:    2379 ms
- Avg wall time:      23.79 ms
- Greske: 0
- Raspodjela:
- user-service:8081: 50
- user-service:8085: 50

---

## DIO 2 â€” report-service â†’ user-service (sinhrona komunikacija)

Scenario: svaki POST /api/prijave uzrokuje sinhroni poziv
UserServiceKlijent.validirajKorisnika() -> GET /api/korisnici/{id}
RestTemplate u report-service je @LoadBalanced (Eureka balansira).

- Total latency (end-to-end kroz gateway): 5237 ms
- Avg latency:  52.37 ms
- Min latency:  36 ms
- Max latency:  288 ms
- Total wall:   5416 ms
- Avg wall:     54.16 ms
- Greske: 0
- Raspodjela user-service instanci:
- user-service: 100
