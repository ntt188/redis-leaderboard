# Redis Leaderboard

A gamer leaderboard built with **Redis Sorted Set** and **Spring Boot**, running on a Redis **master–replica** setup.

## Architecture

```
                 ZADD (write)
Spring Boot  ─────────────────▶  redis-master  :6379
   (Lettuce)                          │
             ◀─────────────────       │ replication
                 ZREVRANGE (read)     ▼
                                 redis-replica :6380  (read-only)
```

- Writes go to the **master**; reads prefer the **replica** (`ReadFrom.REPLICA_PREFERRED`).
- Scores are stored in the Sorted Set key `leaderboard` (member = username, score = points).

## Requirements

- Java 17+
- Maven 3.9+
- Docker

## Run

```bash
# 1. Start Redis master + replica
docker compose up -d

# 2. Start the app (port 8080)
mvn spring-boot:run
```

Check replication:

```bash
docker exec redis-replica redis-cli INFO replication
# role:slave, master_link_status:up
```

## API

### `POST /leaderboard/{username}` — set a user's score (`ZADD`)

```bash
curl -X POST localhost:8080/leaderboard/david \
  -H 'Content-Type: application/json' \
  -d '{"score": 3100}'
```

```json
{"username":"david","score":3100.0}
```

`ZADD` overwrites the previous score of an existing user.

### `GET /leaderboard/top/{n}` — top N gamers (`ZREVRANGE ... WITHSCORES`)

```bash
curl localhost:8080/leaderboard/top/3
```

```json
[
  {"rank":1,"username":"david","score":3100.0},
  {"rank":2,"username":"bob","score":2300.0},
  {"rank":3,"username":"alice","score":1500.0}
]
```

### Errors

| Case | Status | Body |
|---|---|---|
| `score` missing or `null` | 400 | `{"error":"score is required"}` |
| `score` < 0 | 400 | `{"error":"score must be >= 0"}` |
| Invalid JSON | 400 | `{"error":"invalid JSON body"}` |
| `n` not in 1..100 | 400 | `{"error":"n must be between 1 and 100"}` |
| Redis unavailable | 503 | `{"error":"leaderboard is temporarily unavailable"}` |

## Project structure

```
docker-compose.yml               Redis master + replica
src/main/java/com/example/leaderboard/
  RedisConfig.java               Master/replica connection, 2s command timeout
  LeaderboardService.java        ZADD / ZREVRANGE
  LeaderboardController.java     REST endpoints + validation
  ApiExceptionHandler.java       Error responses (400 / 503)
src/main/resources/application.yml   Redis hosts and ports
```

## Notes

- Replication is asynchronous, so a read from the replica right after a write may briefly return stale data. Use `ReadFrom.MASTER` if reads must always be up to date.
- Stop Redis with `docker compose down`.
