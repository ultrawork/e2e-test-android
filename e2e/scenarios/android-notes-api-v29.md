# E2E сценарии: Android v29 — Верификация Retrofit и ViewModel

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
emulator -list-avds
emulator -avd <AVD_NAME> -no-snapshot-load
adb devices
```

### Сборка и установка приложения

```bash
./gradlew assembleDebug -PAPI_BASE_URL="http://10.0.2.2:4000/api/"
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.ultrawork.notes/.MainActivity
```

## Быстрая валидация через curl

```bash
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes
```

## Мониторинг через adb logcat

```bash
adb logcat -s OkHttp:D
adb shell run-as com.ultrawork.notes cat /data/data/com.ultrawork.notes/shared_prefs/*.xml | grep jwt_token
adb logcat --pid=$(adb shell pidof com.ultrawork.notes)
```

---

## Сценарии

### SC-001: POST /api/auth/dev-token — получение токена
- HTTP Status: `200 OK`
- Тело ответа содержит `token`
- AuthInterceptor сохраняет токен в SharedPreferences с ключом `jwt_token`

### SC-002: GET /api/notes с Bearer-токеном — список заметок
- HTTP Status: `200 OK`
- Тело — JSON-массив заметок (id, title, content, createdAt, updatedAt)
- ViewModel обновляет UI через Flow

### SC-003: GET /api/notes без токена — ошибка 401
- HTTP Status: `401 Unauthorized`
- Тело: `{ "error": "Unauthorized" }`
- Приложение перенаправляет на экран авторизации или показывает ошибку

### SC-004: POST /api/notes — создание заметки (201 + id)
- HTTP Status: `201 Created`
- Тело содержит `id`, `title`, `content`, `createdAt`, `updatedAt`
- Заметка появляется в списке после обновления

### SC-005: POST /api/notes без content — ошибка 400
- HTTP Status: `400 Bad Request`
- Тело: `{ "error": "Content is required" }`
- ViewModel обрабатывает ошибку 400

### SC-006: DELETE /api/notes/{id} — успешное удаление (204)
- HTTP Status: `204 No Content`
- Тело: пустое
- Заметка исчезает из списка

### SC-007: DELETE /api/notes/{id} несуществующей заметки — ошибка 404
- HTTP Status: `404 Not Found`
- Тело: `{ "error": "Note not found" }`
- ViewModel обрабатывает ошибку 404
