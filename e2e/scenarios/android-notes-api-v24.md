# E2E Сценарии: Android v24 — Верификация Retrofit-интеграции с backend API

**Версия:** v24
**Дата:** 2026-03-28
**Платформа:** Android (Kotlin, Retrofit + OkHttp)
**Покрытие:** `ApiService`, `AuthInterceptor`, `NotesRepositoryImpl`, `NotesViewModel`

---

## Окружение

| Параметр | Значение |
|---|---|
| Backend | `e2e-test-backend` |
| Порт | `4000` |
| JWT | Enabled (`JWT_ENABLED=true`) |
| JWT Secret | `e2e-test-secret-key-ultrawork` |
| NODE_ENV | `development` (обязательно для SC-001: `/api/auth/dev-token`) |
| Base URL | `http://localhost:4000/api` |

---

## Сценарии

### SC-001: Получение dev-токена

| Поле | Значение |
|---|---|
| ID | SC-001 |
| Название | Получение dev-токена (`POST /api/auth/dev-token`) |
| Предусловие | Backend запущен на порту 4000, `JWT_ENABLED=true`, `NODE_ENV=development` |
| Действие | `POST /api/auth/dev-token` |
| Ожидаемый результат | HTTP 200, тело содержит `{"token": "<JWT>"}` |

```bash
curl -s -X POST http://localhost:4000/api/auth/dev-token
```

---

### SC-002: GET заметок без токена (401)

| Поле | Значение |
|---|---|
| ID | SC-002 |
| Название | Получение списка заметок без авторизации |
| Предусловие | Backend запущен, JWT включён |
| Действие | `GET /api/notes` без заголовка `Authorization` |
| Ожидаемый результат | HTTP 401 Unauthorized |

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes
```

---

### SC-003: GET заметок с валидным токеном (200)

| Поле | Значение |
|---|---|
| ID | SC-003 |
| Название | Получение списка заметок с Bearer-токеном |
| Предусловие | Получен валидный JWT через SC-001 |
| Действие | `GET /api/notes` с заголовком `Authorization: Bearer <token>` |
| Ожидаемый результат | HTTP 200, массив объектов `Note` с полями `id` (string/UUID), `title` (string), `content` (string) |

```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

---

### SC-004: Создание заметки с валидным токеном (201)

| Поле | Значение |
|---|---|
| ID | SC-004 |
| Название | Создание новой заметки (POST) |
| Предусловие | Получен валидный JWT через SC-001 |
| Действие | `POST /api/notes` с телом `{"title":"Test Note v24","content":"Verification content v24"}` |
| Ожидаемый результат | HTTP 201, тело содержит созданную заметку с `id`, `title`, `content` |

```bash
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v24","content":"Verification content v24"}'
```

---

### SC-005: Удаление заметки с валидным токеном (204)

| Поле | Значение |
|---|---|
| ID | SC-005 |
| Название | Удаление существующей заметки (DELETE) |
| Предусловие | Создана заметка через SC-004, получен её `id` |
| Действие | `DELETE /api/notes/:id` с Bearer-токеном |
| Ожидаемый результат | HTTP 204 No Content |

```bash
NOTE_ID="<id из SC-004>"
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  http://localhost:4000/api/notes/$NOTE_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

### SC-006: Создание заметки с пустым payload (400)

| Поле | Значение |
|---|---|
| ID | SC-006 |
| Название | POST заметки без обязательных полей |
| Предусловие | Получен валидный JWT через SC-001 |
| Действие | `POST /api/notes` с телом `{}` |
| Ожидаемый результат | HTTP 400 Bad Request |

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

### SC-007: Удаление несуществующей заметки (404)

| Поле | Значение |
|---|---|
| ID | SC-007 |
| Название | DELETE несуществующей заметки |
| Предусловие | Получен валидный JWT через SC-001 |
| Действие | `DELETE /api/notes/nonexistent-id-99999` |
| Ожидаемый результат | HTTP 404 Not Found |

```bash
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  http://localhost:4000/api/notes/nonexistent-id-99999 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Матрица сценариев

| ID | Сценарий | Метод | Эндпоинт | Ожидаемый код | Компонент Android |
|---|---|---|---|---|---|
| SC-001 | Dev-токен | POST | `/api/auth/dev-token` | 200 | `AuthInterceptor` |
| SC-002 | GET без токена | GET | `/api/notes` | 401 | `AuthInterceptor` |
| SC-003 | GET с токеном | GET | `/api/notes` | 200 | `ApiService`, `NotesRepositoryImpl` |
| SC-004 | POST заметки | POST | `/api/notes` | 201 | `ApiService`, `NotesRepositoryImpl` |
| SC-005 | DELETE заметки | DELETE | `/api/notes/:id` | 204 | `ApiService`, `NotesRepositoryImpl` |
| SC-006 | POST пустой payload | POST | `/api/notes` | 400 | `ApiService` |
| SC-007 | DELETE несуществующей | DELETE | `/api/notes/:id` | 404 | `ApiService` |
