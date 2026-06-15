# FixIt

## Pametna platforma za prijavu komunalnih problema

**FixIt** je moderna mikroservisna web aplikacija namijenjena digitalizaciji procesa prijave i upravljanja komunalnim problemima. Platforma omogućava građanima jednostavno prijavljivanje problema poput oštećenja cesta, kvarova ulične rasvjete, divljih deponija i drugih infrastrukturnih nedostataka, dok lokalnim službama pruža centralizovan sistem za efikasnu obradu, dodjelu i praćenje prijava.

---

## Pregled sistema

Aplikacija je implementirana korištenjem **Spring Cloud** mikroservisne arhitekture i **React/Vite** frontend tehnologije.

```text
                ┌─────────────────────────┐
                │  Frontend (React/Vite)  │
                └────────────┬────────────┘
                             │
                ┌────────────▼────────────┐
                │       API Gateway       │
                │ (Spring Cloud + JWT)    │
                └────────────┬────────────┘
                             │
 ┌───────────────────────────┼───────────────────────────┐
 │                           │                           │
 ▼                           ▼                           ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ user-service │      │management-   │      │report-service│
│              │      │service       │      │              │
└──────┬───────┘      └──────┬───────┘      └──────┬───────┘
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                     ┌───────▼───────┐
                     │notification-  │
                     │service        │
                     └───────────────┘

      Eureka Server (8761) ⇄ RabbitMQ (SAGA Events)
```

---

## Ključne karakteristike

### Mikroservisna arhitektura

Sistem je podijeljen na nezavisne servise koji komuniciraju putem REST API-ja i asinhronih događaja.

### Asinhrona obrada događaja

Promjene statusa prijava implementirane su korištenjem **Choreography SAGA** obrasca uz **RabbitMQ**. Klijent odmah dobija odgovor, dok se konačni rezultat isporučuje asinhrono putem WebSocket konekcije.

### Komunikacija u realnom vremenu

Notifikacije i ažuriranja statusa prijava dostavljaju se korisnicima kroz **Socket.IO / WebSocket** komunikaciju.

### Izolovane baze podataka

Svaki mikroservis koristi vlastitu **MySQL** bazu podataka, čime se postiže labavo povezivanje komponenti i jednostavnije održavanje sistema.

### Sigurnost

Autentifikacija i autorizacija centralizovane su na API Gateway nivou korištenjem **JWT tokena** i **RSA ključeva**.

---

## Tehnologije

### Backend

* Java 21
* Spring Boot
* Spring Cloud Gateway
* Spring Security
* Spring Data JPA
* Eureka Service Discovery
* RabbitMQ
* MySQL

### Frontend

* React
* Vite
* Socket.IO Client

### DevOps

* Docker
* Docker Compose
* GitHub

---

## Pokretanje aplikacije

### Preduslovi

Potrebno je imati instalirano:

* Docker Desktop
* Git
* Java 21
* Maven
* Node.js
* PowerShell (Windows)

---

### 1. Kloniranje repozitorija

```bash
git clone <URL_REPOZITORIJA>
cd FixIt
```

---

### 2. Konfiguracija okruženja

U korijenu projekta kreirati `.env` datoteku:

```env
DB_USERNAME=fixit
DB_PASSWORD=your_password

RABBITMQ_USER=fixit
RABBITMQ_PASS=your_password

JWT_PRIVATE_KEY=<RSA_PRIVATE_KEY_BASE64>
JWT_PUBLIC_KEY=<RSA_PUBLIC_KEY_BASE64>

GATEWAY_SECRET=<SECRET>
```

---

### 3. Pokretanje infrastrukture

Pokrenuti RabbitMQ i baze podataka:

```bash
docker compose up -d
```

Provjera aktivnih kontejnera:

```bash
docker ps
```

---

### 4. Pokretanje aplikacije

Pokretanje svih servisa:

```powershell
./start.ps1
```

---

## Servisi i portovi

| Komponenta           | Port  |
| -------------------- | ----- |
| Frontend             | 3000  |
| API Gateway          | 8080  |
| User Service         | 8081  |
| Management Service   | 8082  |
| Report Service       | 8083  |
| Notification Service | 8084  |
| WebSocket Server     | 9001  |
| Eureka Server        | 8761  |
| RabbitMQ Management  | 15672 |

---

## Testni korisnik

Za inicijalno testiranje sistema:

```text
Email: admin@fixit.ba
Lozinka: Admin12345!
Uloga: ADMIN
```

---

## Ručno pokretanje servisa

### Backend servis

```bash
cd user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Struktura projekta

```text
FixIt
│
├── api-gateway
├── eureka-server
├── user-service
├── management-service
├── report-service
├── notification-service
├── frontend
├── docker-compose.yml
└── start.ps1
```

---

## Autori

Projekat razvijen u okviru univerzitetskog projekta iz predmeta **Napredne web tehnologije**.
