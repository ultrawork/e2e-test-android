# Notes — Android

Android-клиент для кроссплатформенного приложения заметок. Позволяет создавать, редактировать и удалять заметки с синхронизацией через REST API.

## Технологический стек

- **Язык:** Kotlin
- **UI:** Jetpack Compose + Material3
- **Архитектура:** MVVM
- **DI:** Hilt
- **Локальное хранилище:** Room
- **Навигация:** Navigation Compose
- **Сеть:** Retrofit + OkHttp
- **Асинхронность:** Coroutines + Flow
- **Безопасность:** EncryptedSharedPreferences (JWT-токены)
- **Сборка:** Gradle KTS + Version Catalogs

## Начало работы

### Требования

- Android Studio Ladybug (2024.2) или новее
- JDK 17
- Android SDK 35

### Запуск

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/ultrawork/e2e-test-android.git
   cd e2e-test-android
   ```

2. Скопируйте `.env.example` и настройте переменные:
   ```bash
   cp .env.example .env
   ```

3. Укажите `API_BASE_URL` в `gradle.properties` (или передайте при сборке):
   ```properties
   API_BASE_URL=http://10.0.2.2:3000/api
   ```

4. Откройте проект в Android Studio и запустите на эмуляторе или устройстве.

### Сборка APK

```bash
./gradlew assembleDebug
```

### Docker-сборка

```bash
docker build -t notes-android .
```

## Структура проекта

```
app/src/main/java/com/ultrawork/notes/
├── di/                  # Hilt-модули
├── data/
│   ├── local/           # Room (БД, DAO)
│   ├── remote/          # Retrofit (API-сервисы)
│   └── repository/      # Репозитории
├── model/               # Модели данных
├── navigation/          # NavGraph
├── ui/
│   ├── screens/         # Экраны (Composable)
│   ├── components/      # Переиспользуемые UI-компоненты
│   └── theme/           # Material3 тема
├── viewmodel/           # ViewModels
├── MainActivity.kt
└── NotesApp.kt
```

## E2E v24

Верификация API-контракта между Android-клиентом и backend (Retrofit + OkHttp).

### Документы

- **Сценарии:** [`e2e/scenarios/android-notes-api-v24.md`](e2e/scenarios/android-notes-api-v24.md) — 7 E2E-сценариев
- **Отчёт:** [`e2e/reports/android-v24.md`](e2e/reports/android-v24.md) — результаты верификации + GAP-анализ

### Предусловия

1. Запустите backend:
   ```bash
   cd e2e-test-backend
   NODE_ENV=development JWT_ENABLED=true JWT_SECRET=e2e-test-secret-key-ultrawork PORT=4000 npm run dev
   ```

2. Убедитесь, что backend отвечает:
   ```bash
   curl http://localhost:4000/health
   ```

### Запуск curl-тестов

```bash
# 1. Получить dev-токен
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"

# 2. GET без токена → 401
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes

# 3. GET с токеном → 200
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes

# 4. POST заметки → 201
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v24","content":"Verification content v24"}'

# 5. DELETE заметки → 204 (подставьте id из шага 4)
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  http://localhost:4000/api/notes/<NOTE_ID> \
  -H "Authorization: Bearer $TOKEN"

# 6. POST пустой payload → 400
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'

# 7. DELETE несуществующей → 404
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  http://localhost:4000/api/notes/nonexistent-id-99999 \
  -H "Authorization: Bearer $TOKEN"
```

### Интерпретация результатов

- **PASS** — API-эндпоинт возвращает ожидаемый HTTP-код и структуру данных
- **GAP** — код Android-клиента не реализован или не соответствует требованиям (см. таблицу GAP/BUG в отчёте)
