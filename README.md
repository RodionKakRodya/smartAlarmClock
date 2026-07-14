# Smart Alarm Clock

Android-приложение на Kotlin, которое каждые 15 минут проверяет Huawei Health Kit на появление новой записи сна и выставляет системный будильник на 7,5 часов после времени засыпания.

## Как это работает

1. Пользователь нажимает кнопку **«Включить умный будильник»**.
2. `WorkManager` запускает периодическую задачу с минимальным системным интервалом 15 минут.
3. Задача читает последние 36 часов данных `DT_CONTINUOUS_SLEEP` из Huawei Health Kit.
4. Если найдено новое время начала сна, приложение открывает системный `AlarmClock` intent и ставит будильник на `sleepStart + 450 минут`.
5. Обработанное время сна сохраняется в `SharedPreferences`, чтобы не создавать один и тот же будильник повторно.

## Что нужно настроить перед реальным запуском

- Создать приложение в AppGallery Connect.
- Подключить подпись приложения и `agconnect-services.json`.
- Запросить у пользователя разрешения Huawei Health Kit на чтение сна до запуска фоновой проверки.
- Проверить поведение выбранного приложения часов: разные OEM-прошивки могут требовать показ UI для `AlarmClock.ACTION_SET_ALARM`.

## Запуск в Android Studio на Windows

В репозиторий добавлен Gradle Wrapper, поэтому локально установленный `gradle` не нужен.

1. Откройте папку проекта в Android Studio через **File → Open**.
2. Дождитесь **Gradle Sync**.
3. Если запускаете команды из PowerShell, используйте Windows-wrapper:

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

4. В Git Bash или WSL используйте Unix-wrapper:

   ```bash
   ./gradlew :app:assembleDebug
   ```

Если Android Studio попросит установить Android SDK Platform 34 или Build Tools, согласитесь на установку через SDK Manager.

## Если Gradle Sync не сработал

1. Убедитесь, что открыта корневая папка проекта, где находятся `settings.gradle.kts` и `gradlew.bat`, а не папка `app`.
2. В Android Studio откройте **File → Settings → Build, Execution, Deployment → Gradle** и выберите **Gradle JDK = 17**. Для Android Gradle Plugin 8.x не используйте JDK 8 или 11.
3. Проверьте, что Android Studio может скачать зависимости из `google()`, `mavenCentral()` и Huawei Maven. Если вы в сети с proxy/VPN, добавьте proxy в **Settings → Appearance & Behavior → System Settings → HTTP Proxy**.
4. Если ошибка связана с Android SDK 34, откройте **Tools → SDK Manager** и установите **Android API 34**.
5. Если ошибка связана с Huawei Health Kit, проверьте доступность `https://developer.huawei.com/repo/` из браузера и повторите **File → Sync Project with Gradle Files**.
6. Если запускаете сборку из PowerShell, используйте:

   ```powershell
   .\gradlew.bat :app:assembleDebug --stacktrace
   ```

Если после этих шагов синхронизация всё ещё падает, пришлите первую строку после `* What went wrong:` из окна **Build** или **Sync** — по ней можно точно понять причину.


### Ошибка `Task 'assembleDebug' not found in root project`

Эта ошибка означает, что Gradle не видит Android-модуль `:app`. Чаще всего это происходит, если открыта не та папка, используются старые wrapper-файлы или в корне случайно появился другой `settings.gradle` после `gradle init`.

Проверьте в PowerShell из корня проекта:

```powershell
Get-Content .\settings.gradle.kts
Get-Content .\gradle\wrapper\gradle-wrapper.properties
.\gradlew.bat projects
```

В `settings.gradle.kts` должна быть строка `include(":app")`, а команда `projects` должна показать проект `:app`. Если в папке есть лишние файлы `settings.gradle` или `build.gradle`, созданные вручную/через `gradle init`, удалите их или перенесите, потому что они могут скрывать `settings.gradle.kts` проекта.

Для сборки используйте явное имя задачи модуля:

```powershell
.\gradlew.bat :app:assembleDebug --stacktrace
```


### Ошибка `Inconsistent JVM-target compatibility`

Проект явно фиксирует Java и Kotlin на JVM target 17. Если у вас раньше Gradle выбирал Kotlin target 21, после обновления выполните чистую сборку:

```powershell
.\gradlew.bat clean :app:assembleDebug --stacktrace
```

В Android Studio также проверьте **File → Settings → Build, Execution, Deployment → Gradle → Gradle JDK = 17**.

## Как приложение подключается к Huawei Health Kit

Подключение выполняется через зависимость `com.huawei.hms:health` и репозиторий Huawei Maven. В рантайме `HuaweiHealthKitSleepDataSource` получает `DataController` через `HiHealth.getDataController(context)`, формирует `DataReadOptions` для `DataType.DT_CONTINUOUS_SLEEP` за последние 36 часов и вызывает `dataController.read(...)`.

Важно: для реального устройства перед чтением сна нужно настроить приложение в AppGallery Connect, добавить `agconnect-services.json` и реализовать экран/flow запроса разрешений Huawei Health Kit на чтение сна.

## Таблица данных сна

На главном экране есть кнопка **«Показать данные сна»**. Она загружает записи сна из Huawei Health Kit за последние 36 часов и строит таблицу с колонками: начало сна, конец сна и длительность в минутах.


## Если push не проходит из-за бинарных файлов

Некоторые git-хостинги/песочницы не принимают бинарные файлы. Поэтому `gradle-wrapper.jar` не хранится напрямую в репозитории: вместо него есть текстовый файл `gradle/wrapper/gradle-wrapper.jar.base64` и скрипты восстановления.

Перед запуском `gradlew` восстановите wrapper jar локально:

```powershell
.\restore-gradle-wrapper.ps1
.\gradlew.bat :app:assembleDebug --stacktrace
```

В Git Bash/WSL можно выполнить:

```bash
./restore-gradle-wrapper.sh
./gradlew :app:assembleDebug --stacktrace
```

Файл `gradle/wrapper/gradle-wrapper.jar` добавлен в `.gitignore`, поэтому после восстановления он не будет мешать push.
