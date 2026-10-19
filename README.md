# mac-backend

Einfaches Spring-Boot-Backend (Java 17, Maven), das eine PostgreSQL-Tabelle mit Geräten über HTTP bereitstellt und die Suche nach MAC-Adresse erlaubt.

## Datenmodell

Tabelle `geraet` (wird beim Start automatisch angelegt):

| Spalte | Typ | Hinweis |
|---|---|---|
| `id` | bigint | Primärschlüssel, automatisch vergeben |
| `name` | varchar | Gerätename, Pflichtfeld |
| `mac` | varchar | MAC-Adresse, Pflichtfeld, eindeutig |

Die MAC wird so gespeichert, wie sie gesendet wird. Die Suche ignoriert Groß-/Kleinschreibung, aber nicht das Format (`AA:BB:…` ≠ `AA-BB-…`).

## Konfiguration

Der Datenbankzugang steht **nicht** im Repo, sondern in einer externen `application.yml`. Vorlage: [`application.example.yml`](application.example.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5332/macdb
    username: user
    password: geheim
```

Spring Boot findet die Datei automatisch, wenn sie im Arbeitsverzeichnis (`./application.yml`) oder in `./config/` liegt. Alternativ:

```bash
java -jar mac-backend.jar --spring.config.additional-location=/pfad/zur/application.yml
```

`application.yml` und `config/` sind in `.gitignore` eingetragen.

Die Datenbank muss existieren, z. B.:

```bash
docker exec <postgres-container> psql -U user -d postgres -c "CREATE DATABASE macdb"
```

## Bauen und starten

```bash
mvn package
java -jar target/mac-backend-0.0.1-SNAPSHOT.jar
# oder während der Entwicklung:
mvn spring-boot:run
```

Der Server läuft standardmäßig auf Port 8080.

## Docker

Image bauen und starten (die `application.yml` wird nach `/app/config/` gemountet):

```bash
docker build -t mac-backend .
docker run -p 8080:8080 -v /pfad/zum/config-ordner:/app/config:ro mac-backend
```

Läuft Postgres auf dem Host, in der `application.yml` statt `localhost` die Host-Adresse verwenden (oder `--network host`).

### GitHub Action

`.github/workflows/docker.yml` baut das Image und pusht es nach Docker Hub als `<DOCKERHUB_USERNAME>/mac-backend`:

| Auslöser | Tags |
|---|---|
| Push auf `main` | `latest`, `sha-<commit>` |
| Git-Tag `v1.2.3` | `1.2.3`, `sha-<commit>` |
| manuell (`workflow_dispatch`) | je nach Branch/Tag |

Benötigte Repository-Secrets (Settings → Secrets and variables → Actions):

- `DOCKERHUB_USERNAME`: Docker-Hub-Benutzername
- `DOCKERHUB_TOKEN`: Docker-Hub-Access-Token (Docker Hub → Account Settings → Personal access tokens, Recht „Read & Write“)

## API

Basis-Pfad: `/api/geraete`

| Methode | Pfad | Beschreibung | Antwort |
|---|---|---|---|
| GET | `/api/geraete` | alle Geräte | 200 |
| GET | `/api/geraete/{id}` | Gerät per ID | 200 / 404 |
| GET | `/api/geraete/suche?mac=…` | Gerät per MAC | 200 / 404 |
| POST | `/api/geraete` | Gerät anlegen | 201 / 409 bei doppelter MAC |
| PUT | `/api/geraete/{id}` | Gerät ändern | 200 / 404 / 409 |
| DELETE | `/api/geraete/{id}` | Gerät löschen | 204 / 404 |

### Beispiele

```bash
curl -X POST localhost:8080/api/geraete \
  -H 'Content-Type: application/json' \
  -d '{"name":"Drucker","mac":"AA:BB:CC:DD:EE:FF"}'

curl "localhost:8080/api/geraete/suche?mac=aa:bb:cc:dd:ee:ff"
```

Antwort:

```json
{"id":1,"name":"Drucker","mac":"AA:BB:CC:DD:EE:FF"}
```
