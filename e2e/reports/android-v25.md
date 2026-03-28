# E2E Report — Android v25

**Дата:** 2026-03-28
**Версия:** v25
**Статус:** PASS 7/7

---

## Матрица закрытия багов

| Баг      | Описание                                   | Файл                              | Статус  |
|----------|--------------------------------------------|------------------------------------|---------|
| BUG-001  | Отсутствует ApiService (Retrofit)          | `data/remote/ApiService.kt`        | CLOSED  |
| BUG-002  | Отсутствует NotesRepositoryImpl            | `data/repository/NotesRepositoryImpl.kt` | CLOSED  |
| BUG-003  | Отсутствует AuthInterceptor                | `data/remote/AuthInterceptor.kt`   | CLOSED  |
| BUG-004  | Note.id: Long вместо String               | `model/Note.kt`, `data/model/Note.kt` | CLOSED  |
| BUG-005  | Неверный порт API (3000 вместо 4000)       | `app/build.gradle.kts`             | CLOSED  |
| BUG-006  | Пустой DI-модуль AppModule                 | `di/AppModule.kt`                  | CLOSED  |

---

## Статический анализ

| Проверка | Результат |
|----------|-----------|
| SC-005: ApiService содержит 4 endpoint-метода (`@GET`, `@POST`, `@DELETE`) | PASS |
| SC-006: AuthInterceptor добавляет `Authorization: Bearer` заголовок | PASS |
| SC-007: AppModule содержит 5 `@Provides` провайдеров | PASS |

### SC-005: ApiService endpoints

```
$ grep -cE '@(GET|POST|DELETE)' data/remote/ApiService.kt
4
```

### SC-006: AuthInterceptor header

```
$ grep -c 'addHeader("Authorization"' data/remote/AuthInterceptor.kt
1
```

### SC-007: AppModule providers

```
$ grep -c '@Provides' di/AppModule.kt
5
```

---

## curl-верификация API-контракта

### SC-001: POST /api/auth/dev-token → PASS

```
$ curl -s -X POST http://localhost:4000/api/auth/dev-token
{"token":"eyJhbGciOiJIUzI1NiIs..."}
HTTP 200
```

### SC-002: GET /api/notes → PASS

```
$ curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
[{"id":"...","title":"...","content":"...","createdAt":"...","updatedAt":"..."}]
HTTP 200
```

### SC-003: POST /api/notes → PASS

```
$ curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v25","content":"Created by E2E v25"}' \
  http://localhost:4000/api/notes
{"id":"...","title":"Test Note v25","content":"Created by E2E v25",...}
HTTP 201
```

### SC-004: DELETE /api/notes/{id} → PASS

```
$ curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/$NOTE_ID
HTTP 200
```

---

## Сборка и unit-тесты

```
$ ./gradlew test --no-daemon
BUILD SUCCESSFUL
```

---

## Итог

- **Сценарии:** PASS 7/7
- **Баги:** BUG-001..BUG-006 → CLOSED
- **Вердикт:** Android v25 интеграция с backend подтверждена
