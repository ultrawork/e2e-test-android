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
   API_BASE_URL=http://10.0.2.2:4000/api/
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

## E2E v25

Интеграция Android-клиента с backend v25: Retrofit-слой, DI (Hilt), AuthInterceptor.

- **Сценарии:** [e2e/scenarios/android-notes-api-v25.md](e2e/scenarios/android-notes-api-v25.md) (7 сценариев)
- **Отчёт:** [e2e/reports/android-v25.md](e2e/reports/android-v25.md)
- **Статус:** PASS 7/7, BUG-001..BUG-006 CLOSED

### Быстрая curl-верификация

```bash
# Получить dev-токен
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | jq -r '.token')

# Список заметок
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes | jq .

# Создать заметку
curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","content":"Hello"}' \
  http://localhost:4000/api/notes | jq .
```
