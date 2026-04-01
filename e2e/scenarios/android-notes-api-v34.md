# E2E сценарии: Android v34 — Верификация Retrofit и ViewModel

> **Дата:** 2026-04-01
> **Версия backend:** v34
> **Статус:** Верификация совместимости (на базе PASS v33, без изменений логики)

## Параметры окружения

| Параметр | Значение | Описание |
|----------|----------|----------|
| `BuildConfig.API_BASE_URL` | `http://10.0.2.2:4000/api/` | Базовый URL API (со слэшем на конце) |
| Backend port | `4000` | Порт Express.js backend |
| SharedPreferences ключ токена | `jwt_token` | Ключ для хранения JWT в SharedPreferences |
| AuthInterceptor | Bearer из SharedPreferences | Добавляет `Authorization: Bearer <token>` ко всем запросам |

> **Примечание:** Android-эмулятор не имеет доступа к `localhost` хоста напрямую. Адрес `10.0.2.2` проксирует запросы на `localhost` хост-машины. Поэтому `BuildConfig.API_BASE_URL` использует `10.0.2.2`, а curl-команды — `localhost`.

## Предварительные условия

### Запуск backend

```bash
cd e2e-test-backend
JWT_ENABLED=true JWT_SECRET=test-secret PORT=4000 npm run dev
```

Проверка доступности:

```bash
curl -s http://localhost:4000/api/health
# Ожидаемый ответ: {"status":"ok"} или 200
```

### Запуск Android-эмулятора

```bash
# Список доступных AVD
emulator -list-avds

# Запуск эмулятора
emulator -avd <AVD_NAME> -no-snapshot-load

# Проверка подключения
adb devices
```

### Сборка и установка приложения

```bash
# Сборка с кастомным API_BASE_URL (порт 4000, со слэшем)
./gradlew assembleDebug -PAPI_BASE_URL="http://10.0.2.2:4000/api/"

# Установка на эмулятор
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Запуск приложения
adb shell am start -n com.ultrawork.notes/.MainActivity
```

## Быстрая валидация через curl (без эмулятора)

Получение токена и проверка API напрямую:

```bash
# Получение dev-токена
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"

# Проверка списка заметок
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes

# Проверка без токена (ожидаем 401)
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes
```

## Мониторинг через adb logcat

```bash
# Логи OkHttp (запросы/ответы Retrofit)
adb logcat -s OkHttp:D

# Проверка токена в SharedPreferences
adb shell run-as com.ultrawork.notes cat /data/data/com.ultrawork.notes/shared_prefs/*.xml | grep jwt_token

# Все логи приложения
adb logcat --pid=$(adb shell pidof com.ultrawork.notes)
```

---

## Сценарии

### SC-001: POST /api/auth/dev-token — получение токена

**Цель:** Убедиться, что backend возвращает валидный JWT dev-токен.

**Предусловия:** Backend запущен на порту 4000.

**Шаги:**

1. Отправить POST-запрос на `/api/auth/dev-token`.
2. Проверить статус ответа и наличие `token` в теле.

**curl-эквивалент:**

```bash
curl -s -X POST http://localhost:4000/api/auth/dev-token
```

**Ожидаемый результат:**

- HTTP Status: `200 OK`
- Тело ответа содержит `token`:

```json
{
  "token": "<JWT_TOKEN>"
}
```

**Верификация в Android:**

- AuthInterceptor сохраняет полученный токен в SharedPreferences с ключом `jwt_token`.
- Проверка: `adb shell run-as com.ultrawork.notes cat /data/data/com.ultrawork.notes/shared_prefs/*.xml | grep jwt_token`

---

### SC-002: GET /api/notes с Bearer-токеном — список заметок

**Цель:** Убедиться, что авторизованный запрос возвращает список заметок.

**Предусловия:** Токен получен (SC-001).

**Шаги:**

1. Отправить GET-запрос на `/api/notes` с заголовком `Authorization: Bearer <token>`.
2. Проверить статус ответа и формат данных.

**curl-эквивалент:**

```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

**Ожидаемый результат:**

- HTTP Status: `200 OK`
- Тело ответа — JSON-массив заметок:

```json
[
  {
    "id": "uuid",
    "title": "string",
    "content": "string",
    "createdAt": "ISO8601",
    "updatedAt": "ISO8601"
  }
]
```

**Верификация в Android:**

- ViewModel получает список через Retrofit и обновляет UI.
- `adb logcat -s OkHttp:D` показывает `200` ответ на GET `/api/notes`.

---

### SC-003: GET /api/notes без токена — ошибка 401, сброс авторизации

**Цель:** Убедиться, что запрос без авторизации отклоняется, приложение сбрасывает авторизацию и перенаправляет на экран логина.

**Предусловия:** Токен НЕ передаётся / удалён из SharedPreferences.

**Шаги:**

1. Отправить GET-запрос на `/api/notes` без заголовка `Authorization`.
2. Проверить статус ответа.
3. Убедиться, что приложение сбрасывает сохранённый токен и перенаправляет на экран логина.

**curl-эквивалент:**

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes
# Ожидаемый вывод: 401
```

**Ожидаемый результат:**

- HTTP Status: `401 Unauthorized`
- Тело ответа:

```json
{
  "error": "Unauthorized"
}
```

**Верификация в Android:**

- При отсутствии `jwt_token` в SharedPreferences AuthInterceptor не добавляет заголовок.
- Приложение сбрасывает авторизацию (очищает `jwt_token` в SharedPreferences) и перенаправляет на экран логина.
- UI не крашится — переход на логин-экран происходит корректно.

---

### SC-004: POST /api/notes — создание заметки (201 + id)

**Цель:** Убедиться, что авторизованный пользователь может создать заметку.

**Предусловия:** Токен получен (SC-001).

**Шаги:**

1. Отправить POST-запрос на `/api/notes` с телом `{ "title": "Test Note", "content": "Test content" }` и Bearer-токеном.
2. Проверить статус ответа, наличие `id` в теле.

**curl-эквивалент:**

```bash
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Note", "content": "Test content"}'
```

**Ожидаемый результат:**

- HTTP Status: `201 Created`
- Тело ответа содержит `id`, поля возвращаются в camelCase:

```json
{
  "id": "uuid",
  "title": "Test Note",
  "content": "Test content",
  "createdAt": "ISO8601",
  "updatedAt": "ISO8601"
}
```

**Верификация в Android:**

- ViewModel вызывает `ApiService.createNote()` через Retrofit.
- Заметка появляется в списке после обновления.
- `adb logcat -s OkHttp:D` показывает `201` ответ на POST `/api/notes`.

---

### SC-005: POST /api/notes без content — ошибка 400

**Цель:** Убедиться, что backend валидирует обязательное поле `content`.

**Предусловия:** Токен получен (SC-001).

**Шаги:**

1. Отправить POST-запрос на `/api/notes` с телом `{ "title": "Note without content" }` (без поля `content`) и Bearer-токеном.
2. Проверить статус ответа.

**curl-эквивалент:**

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Note without content"}'
# Ожидаемый вывод: 400
```

**Ожидаемый результат:**

- HTTP Status: `400 Bad Request`
- Тело ответа содержит описание ошибки:

```json
{
  "error": "Content is required"
}
```

**Верификация в Android:**

- ViewModel обрабатывает ошибку 400 и отображает сообщение пользователю.
- `adb logcat -s OkHttp:D` показывает `400` ответ.

---

### SC-006: DELETE /api/notes/{id} — успешное удаление (204)

**Цель:** Убедиться, что авторизованный пользователь может удалить существующую заметку.

**Предусловия:** Токен получен (SC-001), заметка создана (SC-004).

**Шаги:**

1. Создать заметку (SC-004) и сохранить её `id`.
2. Отправить DELETE-запрос на `/api/notes/{id}` с Bearer-токеном.
3. Проверить статус ответа.
4. Повторить GET `/api/notes` и убедиться, что заметка отсутствует в списке.

**curl-эквивалент:**

```bash
# Создание заметки для получения ID
NOTE_ID=$(curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "To Delete", "content": "Will be deleted"}' | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

# Удаление
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/$NOTE_ID
# Ожидаемый вывод: 204
```

**Ожидаемый результат:**

- HTTP Status: `204 No Content`
- Тело ответа: пустое.

**Верификация в Android:**

- ViewModel вызывает `ApiService.deleteNote(id)` через Retrofit.
- Заметка исчезает из списка.
- `adb logcat -s OkHttp:D` показывает `204` ответ на DELETE `/api/notes/{id}`.

---

### SC-007: DELETE /api/notes/{id} несуществующей заметки — ошибка 404

**Цель:** Убедиться, что удаление несуществующей заметки возвращает 404.

**Предусловия:** Токен получен (SC-001).

**Шаги:**

1. Отправить DELETE-запрос на `/api/notes/nonexistent-id` с Bearer-токеном.
2. Проверить статус ответа.

**curl-эквивалент:**

```bash
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/00000000-0000-0000-0000-000000000000
# Ожидаемый вывод: 404
```

**Ожидаемый результат:**

- HTTP Status: `404 Not Found`
- Тело ответа:

```json
{
  "error": "Note not found"
}
```

**Верификация в Android:**

- ViewModel обрабатывает ошибку 404 и отображает соответствующее сообщение.
- `adb logcat -s OkHttp:D` показывает `404` ответ.

---

### SC-008: Пустой список заметок и обработка ошибок 5xx

**Цель:** Убедиться, что UI корректно отображает пустой список заметок («No notes yet») и обрабатывает серверные ошибки (5xx) без крашей, отображая ошибки через snackbar/toast/inline.

**Предусловия:** Токен получен (SC-001).

#### Часть A: Пустой список заметок

**Шаги:**

1. Удалить все существующие заметки через DELETE `/api/notes/{id}` (или использовать чистую БД).
2. Отправить GET-запрос на `/api/notes` с Bearer-токеном.
3. Убедиться, что backend возвращает пустой массив `[]`.
4. Проверить, что UI отображает состояние пустого списка.

**curl-эквивалент:**

```bash
# Получить все заметки и удалить каждую
for id in $(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes | grep -o '"id":"[^"]*"' | cut -d'"' -f4); do
  curl -s -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes/$id
done

# Проверить пустой список
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
# Ожидаемый вывод: []
```

**Ожидаемый результат:**

- HTTP Status: `200 OK`
- Тело ответа: `[]`
- UI отображает текст **"No notes yet"** (или локализованный эквивалент).
- Список заметок не показывается; отображается placeholder пустого состояния.

#### Часть B: Обработка ошибок 5xx

**Шаги:**

1. Имитировать серверную ошибку (остановить backend или настроить возврат 500).
2. Открыть экран заметок в приложении.
3. Убедиться, что приложение не крашится.
4. Проверить отображение сообщения об ошибке.

**Имитация через остановку backend:**

```bash
# Остановить backend
# (Ctrl+C в терминале с backend или kill процесса)

# Попытка запроса — connection refused
curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
# Ожидаемый вывод: 000 (connection refused)
```

**Ожидаемый результат:**

- Приложение НЕ крашится (no ANR, no unhandled exception).
- UI отображает сообщение об ошибке через snackbar, toast или inline-текст (например, «Ошибка загрузки» / «Network error»).
- При восстановлении backend повторный запрос (pull-to-refresh или повторная попытка) загружает данные корректно.

**Верификация в Android:**

- `adb logcat --pid=$(adb shell pidof com.ultrawork.notes)` не содержит `FATAL EXCEPTION` или `ANR`.
- UI отображает `error_message` или snackbar с текстом ошибки.
- После перезапуска backend и повторного запроса — список заметок загружается.
