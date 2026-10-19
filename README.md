# mac-backend

Einfaches Spring-Boot-Backend (Java 17, Maven), das eine PostgreSQL-Tabelle mit Geräten über HTTP bereitstellt und die Suche nach MAC-Adresse erlaubt.

## Datenmodell

Tabelle `geraet` (wird beim Start automatisch angelegt):

| Spalte | Typ | Hinweis |
|---|---|---|
| `id` | bigint | Primärschlüssel, automatisch vergeben |
| `name` | varchar | Gerätename, Pflichtfeld |
| `mac` | varchar | MAC-Adresse, Pflichtfeld, eindeutig |

Beim Anlegen und Ändern wird die MAC normalisiert und ohne Trennzeichen in Kleinbuchstaben gespeichert (`aabbccddeeff`). Akzeptiert werden `aa-bb-cc-dd-ee-ff`, `aa:bb:cc:dd:ee:ff` und `aabbccddeeff` (Groß-/Kleinschreibung egal); andere Werte ergeben 400. Die Suche akzeptiert dieselben Schreibweisen.

Bestehende Einträge im alten Format werden von der Suche weiterhin gefunden. Vereinheitlichen lassen sie sich mit:

```sql
UPDATE geraet SET mac = LOWER(REPLACE(REPLACE(mac, '-', ''), ':', ''));
```

## Konfiguration

Der Datenbankzugang kommt aus Umgebungsvariablen:

| Variable | Bedeutung | Standard |
|---|---|---|
| `DB_HOST` | Postgres-Host | `localhost` |
| `DB_PORT` | Postgres-Port | `5432` |
| `DB_NAME` | Datenbankname | `macdb` |
| `DB_USER` | Benutzer | – (Pflicht) |
| `DB_PASSWORD` | Passwort | – (Pflicht) |

Die Variablen werden in [`application.example.yml`](application.example.yml) zur Datenbank-URL zusammengesetzt. Im Docker-Image liegt diese Datei als `/app/config/application.yml` und wird automatisch geladen.

Die Datenbank muss existieren, z. B.:

```bash
docker exec <postgres-container> psql -U user -d postgres -c "CREATE DATABASE macdb"
```

## Bauen und lokal starten

```bash
mvn package
DB_PORT=5332 DB_USER=user DB_PASSWORD=geheim \
  java -jar target/mac-backend-0.0.1-SNAPSHOT.jar --spring.config.additional-location=application.example.yml
```

Der Server läuft standardmäßig auf Port 8080 (änderbar mit `SERVER_PORT`).

## Docker

```bash
docker build -t mac-backend .
docker run -p 8080:8080 \
  -e DB_HOST=db-host -e DB_PORT=5432 -e DB_NAME=macdb \
  -e DB_USER=user -e DB_PASSWORD=geheim \
  mac-backend
```

Läuft Postgres auf demselben Rechner, `localhost` im Container funktioniert nicht: dann `--network host` verwenden oder die Host-Adresse als `DB_HOST` setzen.

### GitHub Action

`.github/workflows/docker.yml` baut das Image und pusht es nach Docker Hub als `<DOCKERHUB_USERNAME>/mac-backend`. Die Action startet **nur von Hand** und **nur auf einem Versions-Tag**:

```bash
git tag v1.2.3
git push origin v1.2.3
gh workflow run docker.yml --ref v1.2.3
```

(oder GitHub → Actions → „Docker Image“ → „Run workflow“ → bei „Use workflow from“ den Tag auswählen)

Das Image bekommt die Tags `1.2.3` und `latest`. Auf einem Branch gestartet bricht die Action mit einer Fehlermeldung ab.

Für die Flux Image-Automation:

```yaml
apiVersion: image.toolkit.fluxcd.io/v1beta2
kind: ImagePolicy
metadata:
  name: mac-backend
spec:
  imageRepositoryRef:
    name: mac-backend
  policy:
    semver:
      range: '>=1.0.0'
```

Benötigte Repository-Secrets (Settings → Secrets and variables → Actions):

- `DOCKERHUB_USERNAME`: Docker-Hub-Benutzername
- `DOCKERHUB_TOKEN`: Docker-Hub-Access-Token (Docker Hub → Account Settings → Personal access tokens, Recht „Read & Write“)

## API

Basis-Pfad: `/api/geraete`

| Methode | Pfad | Beschreibung | Antwort |
|---|---|---|---|
| GET | `/api/geraete` | alle Geräte | 200 |
| GET | `/api/geraete/{id}` | Gerät per ID | 200 / 404 |
| GET | `/api/geraete/suche?mac=…` | Gerät per MAC | 200 / 404 / 400 bei ungültiger MAC |
| POST | `/api/geraete` | Gerät anlegen | 201 / 400 bei ungültiger MAC / 409 bei doppelter MAC |
| PUT | `/api/geraete/{id}` | Gerät ändern | 200 / 400 / 404 / 409 |
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
{"id":1,"name":"Drucker","mac":"aabbccddeeff"}
```
