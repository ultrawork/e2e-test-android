# E2E Scenarios — Android Notes API v25

## SC-001: Получение dev-токена

**Endpoint:** `POST /api/auth/dev-token`
**Ожидаемый результат:** HTTP 200, тело содержит `{ "token": "<jwt>" }`

```bash
curl -s -X POST http://localhost:4000/api/auth/dev-token | jq .
```

---

## SC-002: Получение списка заметок (с авторизацией)

**Endpoint:** `GET /api/notes`
**Заголовок:** `Authorization: Bearer <token>`
**Ожидаемый результат:** HTTP 200, массив заметок `[{ id, title, content, createdAt, updatedAt }]`

```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | jq -r '.token')
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes | jq .
```

---

## SC-003: Создание заметки

**Endpoint:** `POST /api/notes`
**Заголовок:** `Authorization: Bearer <token>`
**Тело:** `{ "title": "Test Note v25", "content": "Created by E2E v25" }`
**Ожидаемый результат:** HTTP 201, объект заметки с `id`, `title`, `content`

```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | jq -r '.token')
curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v25","content":"Created by E2E v25"}' \
  http://localhost:4000/api/notes | jq .
```

---

## SC-004: Удаление заметки

**Endpoint:** `DELETE /api/notes/{id}`
**Заголовок:** `Authorization: Bearer <token>`
**Ожидаемый результат:** HTTP 200/204

```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | jq -r '.token')
NOTE_ID=$(curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"To Delete","content":"Temp"}' \
  http://localhost:4000/api/notes | jq -r '.id')
curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/$NOTE_ID -w "\nHTTP %{http_code}\n"
```

---

## SC-005: Статический анализ — ApiService содержит все 4 endpoint-метода

**Проверка:** `ApiService.kt` содержит `GET notes`, `POST notes`, `DELETE notes/{id}`, `POST auth/dev-token`.

```bash
grep -cE '@(GET|POST|DELETE)' app/src/main/java/com/ultrawork/notes/data/remote/ApiService.kt
# Ожидается: 4
```

---

## SC-006: Статический анализ — AuthInterceptor добавляет Bearer-заголовок

**Проверка:** `AuthInterceptor.kt` содержит `addHeader("Authorization", "Bearer`

```bash
grep -c 'addHeader("Authorization"' app/src/main/java/com/ultrawork/notes/data/remote/AuthInterceptor.kt
# Ожидается: 1
```

---

## SC-007: Статический анализ — AppModule предоставляет полный DI-граф

**Проверка:** `AppModule.kt` содержит провайдеры для SharedPreferences, OkHttpClient, Retrofit, ApiService, NotesRepository.

```bash
grep -c '@Provides' app/src/main/java/com/ultrawork/notes/di/AppModule.kt
# Ожидается: 5
```
