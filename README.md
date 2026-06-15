```markdown
# FixIt – Pametna platforma za prijavu komunalnih problema

**FixIt** je moderna, visoko skalabilna mikroservisna web aplikacija dizajnirana da premosti jaz između građana i lokalne samouprave. Građanima omogućava brzo i jednostavno prijavljivanje komunalnih problema (poput oštećenja na cestama, kvarova ulične rasvjete, divljih deponija ili oštećenja infrastrukture), dok općinskim službama pruža centralizovan, pametan sistem za efikasno upravljanje, dodjelu i rješavanje tih prijava u realnom vremenu.

---

## Arhitektura Sistema

Aplikacija je implementirana korištenjem mikroservisne arhitekture zasnovane na **Spring Cloud** ekosistemu i **React** frontend frameworku.


```

```
                ┌─────────────────────────┐
                │  Frontend (React/Vite)  │
                └────────────┬────────────┘
                             │
                ┌────────────▼────────────┐
                │       API Gateway       │ (Spring Cloud Gateway + JWT)
                └────────────┬────────────┘
                             │
 ┌───────────────────────────┼───────────────────────────┐
 │ :8081                     │ :8082                     │ :8083

```

┌────┴──────────┐           ┌────┴──────────┐           ┌────┴──────────┐
│  user-service │           │  management   │           │ report-service│
│               │           │    -service   │           │               │
└───────┬───────┘           └───────┬───────┘           └───────┬───────┘
│                           │                           │
└───────────────────────────┼───────────────────────────┘
│ :8084 + :9001 (WebSockets)
┌───────▼───────┐
│ notification  │
│   -service    │
└───────────────┘

[ Eureka Server :8761 (Discovery) ]  ⇄  [ RabbitMQ (Async SAGA Choreography) ]

```

### Ključne Karakteristike & Tehnologije
* **Reaktivnost u realnom vremenu:** Asinhrone akcije i promjene statusa prijava implementirane su pomoću **Choreography SAGA** patterna kroz **RabbitMQ**. Klijent trenutno dobija `202 Accepted` odgovor, dok se konačni ishod asinhrono isporučuje na frontend putem **Socket.IO (WebSockets)**.
* **Izolovane baze podataka:** Svaki mikroservis posjeduje vlastitu, izolovanu **MySQL** bazu podataka, osiguravajući potpuni labavi spoj (*loose coupling*).
* **Sigurnost:** Centralizovana autorizacija i autentifikacija na API Gateway nivou koristeći asimetrične **RSA (JWT)** ključeve.

---

##  Pokretanje Aplikacije

Aplikacija se pokreće kombinacijom Docker kontejnera za infrastrukturu i automatizovane PowerShell skripte za same servise.

###  Preduslovi
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) instaliran i pokrenut
* Git
* PowerShell (za Windows korisnike)

### 1. Kloniranje projekta
```bash
git clone <URL_REPOZITORIJA>
cd FixIt

```

### 2. Konfiguracija okruženja (`.env`)

U korijenu projekta kreirajte ili uredite `.env` fajl. Za produkcijsko okruženje obavezno generišite jedinstvene ključeve:

```env
DB_USERNAME=fixit
DB_PASSWORD=<vasa_lozinka>

RABBITMQ_USER=fixit
RABBITMQ_PASS=<vasa_lozinka>

JWT_PRIVATE_KEY=<RSA_private_key_base64>
JWT_PUBLIC_KEY=<RSA_public_key_base64>

GATEWAY_SECRET=<tajni_kljuc>

```

> ** Savjet za produkciju:** RSA ključeve možete generisati prateći ove komande:
> ```bash
> openssl genrsa -out private.pem 2048
> openssl rsa -in private.pem -pubout -out public.pem
> # Base64 enkoding:
> cat private.pem | base64 -w 0
> cat public.pem | base64 -w 0
> 
> ```
> 
> 

### 3. Pokretanje infrastrukture (Docker)

Prije pokretanja same aplikacije, potrebno je podići baze podataka i message broker. Pokrenite RabbitMQ i MySQL baze kroz Docker:

```bash
# Pokretanje RabbitMQ servera sa management panelom
docker run -d --name rabbitmq \ -p 15672:15672 \ -p 5672:5672 \ rabbitmq:3-management

# Pokretanje izolovanih baza podataka i Eureka servera
docker compose up -d mysql-users mysql-reports mysql-management mysql-notifications eureka-server

```

### 4. Pokretanje servisa putem skripte

Nakon što su baze i RabbitMQ uspješno podignuti i aktivni, pokrenite kompletnu aplikaciju (mikroservise i frontend) pokretanjem pripremljene PowerShell skripte:

```powershell
./start.ps1

```

---

## Pregled Pristupa i Portova

Nakon uspješnog pokretanja, komponente sistema su dostupne na sljedećim adresama:

| Komponenta | Tehnologija | URL |
| --- | --- | --- |
| **Korisnički Interfejs** | React / Vite | [http://localhost:3000](https://www.google.com/search?q=http://localhost:3000) |
| **API Gateway** | Spring Cloud Gateway | [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080) |
| **Service Discovery** | Netflix Eureka | [http://localhost:8761](https://www.google.com/search?q=http://localhost:8761) |
| **Message Broker Panel** | RabbitMQ Management | [http://localhost:15672](https://www.google.com/search?q=http://localhost:15672) <br>

<br> *(User: `fixit` / Pass: `fixit_rabbit_2024`)* |

### Testni Korisnički Podaci (Seed)

Za inicijalno testiranje sistema možete koristiti predefinisanog administratorskog korisnika:

* **Email:** `admin@fixit.ba`
* **Lozinka:** `Admin12345!`
* **Uloga:** `ADMIN`

---

## Razvojno Pokretanje (Manuelno)

Ako ne želite koristiti `start.ps1` skriptu, svaki Spring Boot servis možete pokrenuti pojedinačno iz njegovog direktorija uz `local` profil:

```bash
cd user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

```

Za pokretanje frontenda manuelno:

```bash
cd frontend
npm install
npm run dev

```

---

## Rješavanje Čestih Problema (Troubleshooting)

* **Port je zauzet:** Ako dobijete grešku da je port zauzet, provjerite da nemate pokrenute lokalne instance MySQL-a (3306) ili RabbitMQ-a van Dockera. Ugasite te servise ili promijenite mapiranje portova u Docker komandama.
* **Saga ne prolazi (Notifikacije ne stižu):** Osigurajte da je RabbitMQ kontejner u potpunosti startovan i dostupan prije nego što pokrenete servise preko `start.ps1` skripte. Status možete provjeriti kroz `docker ps`.

```

```