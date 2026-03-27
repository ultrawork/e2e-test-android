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

## Запуск backend v23 и E2E верификация

### 1. Запуск backend

```bash
git clone https://github.com/ultrawork/e2e-test-backend.git
cd e2e-test-backend
git checkout epic/01a22c8f

# Запуск через docker-compose (рекомендуется)
JWT_SECRET=e2e-test-secret-key-ultrawork \
JWT_ENABLED=true \
NODE_ENV=development \
docker-compose up --build
# Backend доступен на http://localhost:4000

# Проверить доступность
curl http://localhost:4000/health
# → {"status":"ok"}
```

### 2. Получение токена

```bash
curl -X POST http://localhost:4000/api/auth/dev-token
# → {"token":"eyJ..."}

# Сохранить токен в переменную
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
```

### 3. Выполнение curl-сценариев (SC-001..SC-007)

```bash
# SC-001: dev-token
curl -s -X POST http://localhost:4000/api/auth/dev-token

# SC-002: без токена → 401
curl -s -o /dev/null -w "%{http_code}" http://localhost:4000/api/notes

# SC-003: с токеном → 200
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes

# SC-004: создание заметки → 201
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note v23","content":"Test Note v23"}'

# SC-005: удаление → 204
curl -s -o /dev/null -w "%{http_code}" -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/$NOTE_ID

# SC-006: пустой payload → 400
curl -s -X POST http://localhost:4000/api/notes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'

# SC-007: несуществующая заметка → 404
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:4000/api/notes/nonexistent-id-99999
```

Полные сценарии: [`e2e/scenarios/android-notes-api.md`](e2e/scenarios/android-notes-api.md)
Отчёт v23: [`e2e/reports/android-v23.md`](e2e/reports/android-v23.md)

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
