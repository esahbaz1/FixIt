# FixIt – Platforma za prijavu komunalnih problema

FixIt je mikroservisna web aplikacija koja građanima omogućava prijavu komunalnih problema (oštećenja cesta, kvara infrastrukture, itd.), a općinskim službama efikasno upravljanje i rješavanje tih prijava.

## Arhitektura

```
Frontend (React/Vite)
      │
API Gateway (Spring Cloud Gateway + JWT)
      │
 ┌────┴────────────────────────────────┐
 │          Mikroservisi               │
 │  user-service      :8081            │
 │  management-service :8082           │
 │  report-service     :8083           │
 │  notification-service :8084 + :9001 │
 └────────────────────────────────────┘
      │
Eureka Server (service discovery)  :8761
RabbitMQ (async SAGA komunikacija)
MySQL (4 odvojene baze)
```

Asinhrone akcije (promjena statusa prijave) koriste **Choreography SAGA** pattern putem RabbitMQ-a. Klijent dobiva `202 Accepted` odmah, a rezultat stigne kao Socket.IO notifikacija.


## Pokretanje putem Dockera

### Preduslovi

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (ili Docker Engine + Compose)
- Git

### Kloniranje projekta

```bash
git clone <URL_REPOZITORIJA>
cd FixIt
```

### Konfiguracija (.env)

Projekt dolazi s `.env` fajlom koji sadrži defaultne vrijednosti za lokalni razvoj. U produkciji obavezno promijeniti:

```env
DB_USERNAME=fixit
DB_PASSWORD=<vasa_lozinka>

RABBITMQ_USER=fixit
RABBITMQ_PASS=<vasa_lozinka>

JWT_PRIVATE_KEY=<RSA_private_key_base64>
JWT_PUBLIC_KEY=<RSA_public_key_base64>

GATEWAY_SECRET=<tajni_kljuc>
```

> **Napomena:** RSA ključevi su već generisani u `.env`. Za produkciju generirajte nove:
> ```bash
> openssl genrsa -out private.pem 2048
> openssl rsa -in private.pem -pubout -out public.pem
> # Base64 encode:
> cat private.pem | base64 -w 0
> cat public.pem | base64 -w 0
> ```

### Pokretanje

```bash
# Izgradnja i pokretanje svih servisa
docker compose up --build

# Ili u pozadini
docker compose up --build -d
```

Prvo pokretanje može trajati **5–10 minuta** jer se grade Docker slike i čeka zdravlje svih servisa.

### Pristup aplikaciji

| Servis | URL |
|---|---|
| **Frontend** | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (fixit / fixit_rabbit_2024) |

### Podrazumijevani korisnici (seed podaci)

| Email | Lozinka | Uloga |
|---|---|---|
| admin@fixit.ba | Admin12345! | ADMIN |

### Zaustavljanje

```bash
# Zaustavi kontejnere
docker compose down

# Zaustavi i obrisi volumene (resetuje baze)
docker compose down -v
```

### Uobičajeni problemi

**Servisi ne startuju:** Sačekajte da MySQL i RabbitMQ budu potpuno zdravi. Prati se statusom:
```bash
docker compose ps
docker compose logs -f report-service
```

**Port zauzet:** Ako je neka lokalna aplikacija na portovima 3000, 8080–8084, 8761, 5672 ili 15672, zaustavite je ili promijenite port u `docker-compose.yml`.

## Razvoj (bez Dockera)

Svaki mikroservis ima `application-local.properties` za lokalno pokretanje. Potrebno je pokrenuti MySQL i RabbitMQ lokalno (ili via Docker samo za infrastrukturu):

```bash
docker compose up mysql-users mysql-reports mysql-management mysql-notifications rabbitmq eureka-server
```

Pa onda svaki Spring Boot servis:
```bash
cd user-service && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Frontend:
```bash
cd frontend && npm install && npm run dev
```
