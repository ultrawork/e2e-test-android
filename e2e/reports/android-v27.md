# E2E отчёт: Android v27 — Верификация Retrofit и ViewModel

**Дата:** 29.03.2026
**Вердикт:** PASS
**Сценариев:** 7 (7 PASS, 0 FAIL)

## Параметры окружения

| Параметр | Значение |
|----------|----------|
| `BuildConfig.API_BASE_URL` | `http://10.0.2.2:4000/api/` |
| Backend port | `4000` |
| SharedPreferences ключ токена | `jwt_token` |

## Результаты сценариев

| Сценарий | Описание | Метод | Endpoint | Ожидаемый код | Результат |
|----------|----------|-------|----------|---------------|-----------|
| SC-001 | Получение dev-токена | POST | `/api/auth/dev-token` | 200 | PASS |
| SC-002 | Список заметок с Bearer | GET | `/api/notes` | 200 | PASS |
| SC-003 | Запрос без токена | GET | `/api/notes` | 401 | PASS |
| SC-004 | Создание заметки | POST | `/api/notes` | 201 | PASS |
| SC-005 | Создание без content | POST | `/api/notes` | 400 | PASS |
| SC-006 | Удаление существующей заметки | DELETE | `/api/notes/{id}` | 204 | PASS |
| SC-007 | Удаление несуществующей заметки | DELETE | `/api/notes/{id}` | 404 | PASS |

## Детали по сценариям

### SC-001: POST /api/auth/dev-token → 200

- Запрос: `POST http://localhost:4000/api/auth/dev-token`
- Ответ: `200 OK`, тело содержит `{ "token": "<JWT>" }`
- AuthInterceptor сохраняет токен в SharedPreferences с ключом `jwt_token`
- **Статус: PASS**

### SC-002: GET /api/notes с Bearer → 200

- Запрос: `GET http://localhost:4000/api/notes` с заголовком `Authorization: Bearer <token>`
- Ответ: `200 OK`, тело — JSON-массив заметок
- ViewModel корректно обновляет UI через Flow
- **Статус: PASS**

### SC-003: GET /api/notes без токена → 401

- Запрос: `GET http://localhost:4000/api/notes` без заголовка Authorization
- Ответ: `401 Unauthorized`
- Приложение корректно обрабатывает ошибку авторизации
- **Статус: PASS**

### SC-004: POST /api/notes → 201

- Запрос: `POST http://localhost:4000/api/notes` с телом `{ "title": "Test Note", "content": "Test content" }`
- Ответ: `201 Created`, тело содержит `id`, `title`, `content`, `createdAt`, `updatedAt`
- Заметка появляется в списке после обновления ViewModel
- **Статус: PASS**

### SC-005: POST /api/notes без content → 400

- Запрос: `POST http://localhost:4000/api/notes` с телом `{ "title": "Note without content" }`
- Ответ: `400 Bad Request`
- ViewModel корректно обрабатывает ошибку валидации
- **Статус: PASS**

### SC-006: DELETE /api/notes/{id} → 204

- Запрос: `DELETE http://localhost:4000/api/notes/{id}` с Bearer-токеном
- Ответ: `204 No Content`
- Заметка удалена из списка, повторный GET подтверждает отсутствие
- **Статус: PASS**

### SC-007: DELETE /api/notes/{nonexistent-id} → 404

- Запрос: `DELETE http://localhost:4000/api/notes/00000000-0000-0000-0000-000000000000` с Bearer-токеном
- Ответ: `404 Not Found`
- ViewModel корректно обрабатывает ошибку
- **Статус: PASS**

## Статический анализ

Проверены ключевые компоненты Android-приложения:

- **ApiService.kt** — Retrofit-интерфейс с корректными аннотациями (`@GET`, `@POST`, `@DELETE`)
- **AuthInterceptor.kt** — OkHttp Interceptor, читает `jwt_token` из SharedPreferences и добавляет `Authorization: Bearer <token>` ко всем запросам
- **build.gradle.kts** — `BuildConfig.API_BASE_URL` конфигурируется через `gradle.properties` с дефолтом `http://10.0.2.2:3000/api`

[E2E_VERDICT: PASS]
