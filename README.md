# Stupid Music 🎵

> Музыка без цензуры. Потому что блокировки — это тупо.

Android-приложение для стриминга музыки через YouTube (Invidious API). Работает в России без VPN.

## Возможности

- 🔍 Поиск треков по названию или артисту
- 🔥 Лента трендовых треков
- ▶️ Фоновое воспроизведение (даже с выключенным экраном)
- 🎨 Material You — подстраивается под цвета твоих обоев
- 🚫 Без рекламы, без цензуры, без пропаганды

## Как работает

Приложение использует **Invidious** — открытый альтернативный фронтенд YouTube. Сервера Invidious не заблокированы в России, поэтому VPN не нужен.

## Сборка

### Через GitHub Actions (рекомендуется)

1. Форкни репо или создай новое
2. Запушь код
3. Actions автоматически соберёт APK
4. Скачай из вкладки **Actions → Artifacts**

### Вручную

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Установка APK

1. Скачай `app-debug.apk` из Actions
2. На телефоне: **Настройки → Безопасность → Установка из неизвестных источников** → разрешить
3. Открой APK и установи

## Стек

- Kotlin + Jetpack Compose
- Hilt (DI)
- Retrofit + kotlinx.serialization
- ExoPlayer / Media3 (фоновое воспроизведение)
- Coil (загрузка изображений)
- Material You (динамические цвета)

## Смена Invidious-инстанса

Если текущий инстанс не работает, измени URL в `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "INVIDIOUS_BASE_URL", "\"https://invidious.io.lol/\"")
```

Список публичных инстансов: https://api.invidious.io/instances.json

---

*Название "Stupid Music" — потому что блокировки музыки это тупо.*
