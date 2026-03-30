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

## E2E v27: Верификация Retrofit и ViewModel

### Переменные окружения

| Параметр | Значение | Описание |
|----------|----------|----------|
| `BuildConfig.API_BASE_URL` | `http://10.0.2.2:4000/api/` | Базовый URL API (со слэшем на конце) |
| SharedPreferences ключ | `jwt_token` | Ключ хранения JWT-токена |
| Backend port | `4000` | Порт Express.js backend |

### Запуск backend

```bash
cd e2e-test-backend
JWT_ENABLED=true JWT_SECRET=test-secret PORT=4000 npm run dev
```

### Сборка и запуск Android-приложения

```bash
# Сборка с указанием порта 4000
./gradlew assembleDebug -PAPI_BASE_URL="http://10.0.2.2:4000/api/"

# Установка на эмулятор
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Запуск
adb shell am start -n com.ultrawork.notes/.MainActivity
```

### Быстрая валидация через curl

```bash
# Получение dev-токена
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Проверка списка заметок
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

### Сценарии и отчёт

- Сценарии (7 шт.): [`e2e/scenarios/android-notes-api-v27.md`](e2e/scenarios/android-notes-api-v27.md)
- Отчёт: [`e2e/reports/android-v27.md`](e2e/reports/android-v27.md)

## E2E v28: Верификация Retrofit и ViewModel

### Переменные окружения

| Параметр | Значение | Описание |
|----------|----------|----------|
| `BuildConfig.API_BASE_URL` | `http://10.0.2.2:4000/api/` | Базовый URL API (со слэшем на конце) |
| SharedPreferences ключ | `jwt_token` | Ключ хранения JWT-токена |
| Backend port | `4000` | Порт Express.js backend |

### Запуск backend

```bash
cd e2e-test-backend
JWT_ENABLED=true JWT_SECRET=test-secret PORT=4000 npm run dev
```

### Сборка и запуск Android-приложения

```bash
# Сборка с указанием порта 4000
./gradlew assembleDebug -PAPI_BASE_URL="http://10.0.2.2:4000/api/"

# Установка на эмулятор
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Запуск
adb shell am start -n com.ultrawork.notes/.MainActivity
```

### Быстрая валидация через curl

```bash
# Получение dev-токена
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Проверка списка заметок
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

### Сценарии и отчёт

- Сценарии (7 шт.): [`e2e/scenarios/android-notes-api-v28.md`](e2e/scenarios/android-notes-api-v28.md)
- Отчёт: [`e2e/reports/android-v28.md`](e2e/reports/android-v28.md)

## E2E v29: Верификация Retrofit и ViewModel

### Переменные окружения

| Параметр | Значение | Описание |
|----------|----------|----------|
| `BuildConfig.API_BASE_URL` | `http://10.0.2.2:4000/api/` | Базовый URL API (со слэшем на конце) |
| SharedPreferences ключ | `jwt_token` | Ключ хранения JWT-токена |
| Backend port | `4000` | Порт Express.js backend |

### Запуск backend

```bash
cd e2e-test-backend
JWT_ENABLED=true JWT_SECRET=test-secret PORT=4000 npm run dev
```

### Сборка и запуск Android-приложения

```bash
# Сборка с указанием порта 4000
./gradlew assembleDebug -PAPI_BASE_URL="http://10.0.2.2:4000/api/"

# Установка на эмулятор
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Запуск
adb shell am start -n com.ultrawork.notes/.MainActivity
```

### Быстрая валидация через curl

```bash
# Проверка доступности API
curl -f -X GET http://10.0.2.2:4000/api/

# Получение dev-токена
TOKEN=$(curl -s -X POST http://localhost:4000/api/auth/dev-token | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Проверка списка заметок
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:4000/api/notes
```

### Сценарии и отчёт

- Сценарии (7 шт.): [`e2e/scenarios/android-notes-api-v29.md`](e2e/scenarios/android-notes-api-v29.md)
- Отчёт: [`e2e/reports/android-v29.md`](e2e/reports/android-v29.md)
