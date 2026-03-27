# E2E Сценарии: Android v23 — Верификация Retrofit-интеграции с backend API

> Покрывает: `ApiService`, `AuthInterceptor`, `NotesRepository`, `NotesViewModel` (v23, актуализировано 2026-03-27)
> Backend: `http://localhost:4000` (порт настраивается через `API_BASE_URL`)
> Метод верификации: HTTP/curl

## Предусловия

| # | Условие |
|---|---------|
| 1 | Backend запущен: `curl http://localhost:4000/health` возвращает `{"status":"ok"}` |
| 2 | `JWT_ENABLED=true`, `JWT_SECRET=e2e-test-secret-key-ultrawork`, `NODE_ENV=development` |
| 3 | Переменная `TOKEN` установлена (см. SC-001) |

## Сценарии

---

### SC-001: Получение dev-токена

**Цель:** Убедиться, что `/api/auth/dev-token` выдаёт валидный JWT.

**Команда:**
```bash
curl -s -X POST http://localhost:4000/api/auth/dev-token
```

**Ожидаемый результат:**
- HTTP статус: `200 OK`
- Тело ответа содержит поле `token` — строка формата `xxxxx.yyyyy.zzzzz` (JWT, 3 части)

**Пример ответа:**
```json
{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJkZXYtdXNlci1pZCIsImVtYWlsIjoiZGV2QGxvY2FsaG9zdCIsImlhdCI6MTcxMTUzNjAwMCwiZXhwIjoxNzExNjIyNDAwfQ.xxxxx"}
```

**Вспомогательная команда (сохранить токен):**
```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo $TOKEN
```

---

### SC-002: GET /api/notes без токена → 401

**Цель:** Убедиться, что `AuthInterceptor` / backend отклоняет неавторизованные запросы.

**Команда:**
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes
```

**Ожидаемый результат:**
- HTTP статус: `401 Unauthorized`
- Тело ответа: `{"error":"Unauthorized"}`

**Проверка тела:**
```bash
curl -s http://localhost:4000/api/notes
```

---

### SC-003: GET /api/notes с валидным токеном → 200 + корректная структура

**Цель:** Убедиться, что запрос с `Authorization: Bearer <token>` возвращает список заметок с полями `id` (String), `title`, `content`.

**Команда:**
```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

**Ожидаемый результат:**
- HTTP статус: `200 OK`
- Тело: массив JSON (`[]` если заметок нет)
- Каждый элемент содержит: `id` (строка), `title` (строка), `content` (строка)

**Проверка с jq (если установлен):**
```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes | jq '.[0] | {id, title, content}'
```

---

### SC-004: POST /api/notes — создание заметки → 201

**Цель:** Убедиться, что создание заметки возвращает `201` и корректные поля `id` (String), `title`, `content`.

**Команда:**
```bash
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v23","content":"Test Note v23"}'
```

**Ожидаемый результат:**
- HTTP статус: `201 Created`
- Тело содержит: `id` (строка, UUID), `title: "Test Note v23"`, `content: "Test Note v23"`

**Сохранить ID заметки:**
```bash
NOTE_ID=$(curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v23","content":"Test Note v23"}' | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo $NOTE_ID
```

---

### SC-005: DELETE /api/notes/:id → 204

**Цель:** Убедиться, что удаление существующей заметки возвращает `204 No Content`.

**Команда:**
```bash
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/$NOTE_ID
```

**Ожидаемый результат:**
- HTTP статус: `204 No Content`
- Тело пустое

---

### SC-006: POST /api/notes с пустым payload → 400

**Цель:** Убедиться, что API возвращает ошибку валидации при отсутствии обязательных полей.

**Команда:**
```bash
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Ожидаемый результат:**
- HTTP статус: `400 Bad Request`
- Тело: `{"error":"title and content are required"}`

---

### SC-007: DELETE несуществующей заметки → 404

**Цель:** Убедиться, что попытка удалить несуществующую заметку возвращает `404 Not Found`.

**Команда:**
```bash
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/nonexistent-id-99999
```

**Ожидаемый результат:**
- HTTP статус: `404 Not Found`
- Тело: `{"error":"Note not found"}`

---

## Итоговая матрица сценариев

| # | Сценарий | Метод | Путь | Статус | Тело |
|---|----------|-------|------|--------|------|
| SC-001 | Получение dev-токена | POST | `/api/auth/dev-token` | 200 | `{token: "<jwt>"}` |
| SC-002 | Без токена — отказ | GET | `/api/notes` | 401 | `{error: "Unauthorized"}` |
| SC-003 | С токеном — список | GET | `/api/notes` | 200 | `[{id: String, title, content}]` |
| SC-004 | Создание заметки | POST | `/api/notes` | 201 | `{id: String, title, content}` |
| SC-005 | Удаление заметки | DELETE | `/api/notes/:id` | 204 | — |
| SC-006 | Пустой payload — ошибка | POST | `/api/notes` | 400 | `{error: "title and content are required"}` |
| SC-007 | Удаление несуществующей | DELETE | `/api/notes/:nonexistent` | 404 | `{error: "Note not found"}` |
