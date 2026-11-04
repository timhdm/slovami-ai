# Slovami AI — локальная сборка и установка Debug APK

## Обязательный порядок

Для ежедневной разработки использовать **локальный Gradle** и **ADB**. GitHub Actions `Android checks` запускать для финальной проверки Pull Request. APK из Actions не нужен для ежедневной установки.

```text
Рабочая ветка → локальный Gradle → Debug APK → ADB → телефон
             → Pull Request → Android checks → подтверждение → Squash and Merge
```

## Конфигурация Windows

- Репозиторий: `timhdm/slovami-ai`
- Клон: `E:\Projects\AI\slovami-ai\slovami-ai`
- SDK: `E:\Projects\AI\slovami-ai\android-sdk`
- Скрипт: `scripts/install-latest-debug.ps1`
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Java: JDK 17, доступный Gradle
- Git for Windows с Git Bash (репозиторий содержит `gradlew`, но не `gradlew.bat`)
- `sherpa-onnx.aar`: скачивается при отсутствии и проверяется SHA-256.

## Ключ подписи — обязательно

GitHub Actions подписывает Debug APK постоянным ключом. **Локальная сборка должна использовать этот же ключ**, иначе `adb install -r` может завершиться ошибкой несовместимой подписи.

Перед запуском установить переменные окружения PowerShell:

```powershell
$env:DEBUG_KEYSTORE_PATH = 'ПОЛНЫЙ-ПУТЬ-К-ЛОКАЛЬНОМУ-JKS'
$env:DEBUG_KEYSTORE_PASSWORD = 'ПАРОЛЬ-ХРАНИЛИЩА'
$env:DEBUG_KEY_PASSWORD = 'ПАРОЛЬ-КЛЮЧА'
```

Ключ `slovami-ai-debug` должен соответствовать CI. Не сохранять пароли и JKS в Git, логах или инструкциях. Получить оригинальный JKS и пароли из защищённого хранилища проекта; если их нет, сначала согласовать миграцию подписи. Скрипт не подменяет ключ автоматически.

## Сборка и установка

Сначала проверить и обновить локальный клон. Не переключать ветку при несохранённых изменениях.

```powershell
cd 'E:\Projects\AI\slovami-ai\slovami-ai'
git status --short
git fetch origin
git switch feat/electric-violet-downloader-v2
git pull --ff-only
& '.\scripts\install-latest-debug.ps1' -Branch 'feat/electric-violet-downloader-v2'
```

Скрипт собирает **локальную текущую ветку**, проверяет чистоту рабочего дерева, скачивает и проверяет AAR, проверяет наличие ключа подписи, выполняет `gradlew.bat assembleDebug` и `adb install -r`. Он не переключает ветки, не очищает данные приложения и не обращается к GitHub Actions.

Дополнительные параметры: `-RunTests` запускает также `testDebugUnitTest`; `-BuildOnly` создаёт APK без установки.

## Проверка перед слиянием

1. Выполнить локальную сборку и проверить приложение на телефоне.
2. Создать Pull Request и дождаться успешного `Android checks`.
3. После подтверждения пользователя выполнить только **Squash and Merge**.
4. Удалить рабочую ветку после слияния.

Одна задача — одна ветка — один итоговый коммит в `main`. Не смешивать изменения скриптов с задачей редизайна.

## Примечание

Имя `install-latest-debug.ps1` сохранено для совместимости, но теперь скрипт **собирает APK локально**. Параметр `-Branch` обязателен и должен совпадать с текущей локальной веткой. Команды GitHub Actions для обычной установки больше не предлагать.
