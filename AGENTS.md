# Работа с проектом в Codex

## Подтверждённые факты и карта кода

- Slovami AI — Android-приложение для записи/импорта аудио, офлайн-расшифровки RU/EN, определения говорящих, резюме и экспорта DOCX. Обзор: [README.md](README.md); границы компонентов: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
- Разработчик и сопровождающий — Timur M. Omarov (TimHdm). Сохранять атрибуцию авторов сторонних моделей и библиотек отдельно от авторства приложения.
- Единственный Gradle-модуль — `:app`. Kotlin 2.0.21, AGP 8.6.1, Gradle Wrapper 8.10.2, JDK 17, Compose/Material 3, coroutines/StateFlow. Android: minSdk 26, compileSdk/targetSdk 34; ABI ARM64 и ARMv7. Источники версий: [build.gradle.kts](build.gradle.kts), [app/build.gradle.kts](app/build.gradle.kts), [gradle/wrapper/gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties).
- Код расположен в `app/src/main/java/app/slovami/ai/`: `MainActivity` переключает экраны; `ui/InterviewViewModel.kt` координирует сценарии; `ui/` содержит Compose UI; `audio/` — аудио и foreground services; `asr/` — sherpa-onnx; `downloader/` — модели; `summary/` — NER/LexRank; `docx/` — собственный генератор DOCX.
- `data/SessionStore.kt` хранит JSON и ссылки на аудио в приватных файлах приложения, без SQL. Блокирующие операции хранилища должны вызываться в `Dispatchers.IO`. Автобэкап и перенос исключены правилами в `app/src/main/res/xml/` и манифестом.
- Резюме работает локально через NER + LexRank и правила. Для него не нужны LLM, GGUF или отдельная JNI-библиотека.
- По решению владельца удалён весь неиспользуемый PRO-задел; не восстанавливать его при обычных исправлениях. Каталог моделей и проверку размещённых файлов сверять с `downloader/ModelRegistry.kt` и `tools/mirror-models.py`; старой утилиты частичного манифеста больше нет.

## Существующие правила проекта

- Следовать [CONTRIBUTING.md](CONTRIBUTING.md): официальный стиль Kotlin (4 пробела), по возможности stateless composables и состояние во ViewModel; строки UI через ресурсы, обновлять RU `values/strings.xml` и EN `values-en/strings.xml`; комментарии — на языке окружающего кода.
- Сохранять офлайн-обработку и приватность. Новые сетевые функции требуют обоснования и opt-in; новые разрешения и тяжёлые зависимости — явной пользы. Не добавлять записи пользователей, секреты, ключи подписи или большие модели в Git. Учитывать [.gitignore](.gitignore) и [models/CREDITS.md](models/CREDITS.md).
- Не менять адреса загрузки моделей до готовности проверенного зеркала; при изменении моделей согласовывать публикацию и SHA-256. Реестр: `downloader/ModelRegistry.kt`; сведения о размещении: [models/MODEL-HOSTING-MIGRATION.md](models/MODEL-HOSTING-MIGRATION.md).
- Для ежедневной разработки принят локальный Gradle + ADB, CI — финальная проверка PR. Основной сценарий: [slovami-ai-build-and-install-instructions.md](slovami-ai-build-and-install-instructions.md). Скрипт собирает текущую локальную ветку, требует чистое дерево и совпадающий с CI debug-ключ; не меняет ветку и не очищает данные приложения.
- Владелец параллельно открывает этот же проект в Android Studio для визуальной проверки и просмотра правок. Сохранять работоспособность Compose Preview; не менять IDE-настройки, не закрывать IDE и не очищать её кеши без отдельной задачи. Не считать сборку CLI визуальной проверкой в Android Studio или на устройстве.
- Комментарии предназначены для разработчиков. Оставлять причины решений, ограничения Android/моделей, единицы измерения, форматы данных и условия безопасного изменения кода. Убирать устаревшие схемы UI, пересказ очевидного кода, декоративные рамки и неподтверждённые оценки скорости. Архитектуру описывать в `docs/ARCHITECTURE.md`; текущий UI смотреть в Android Studio/Compose Preview. При правках обновлять связанные комментарии. Ориентиры по сохранённым пояснениям: [docs/CODE_COMMENTS.md](docs/CODE_COMMENTS.md).
- Процесс PR и публикации: [HOW_TO_PUBLISH.md](HOW_TO_PUBLISH.md), уточнение о подтверждении пользователя перед Squash and Merge — в локальной инструкции выше. Не менять ключ подписи и не распространять debug APK как production release.

## Команды и проверка результата

Из корня проекта, JDK 17 + Android SDK 34; `ANDROID_HOME` либо локальный, неотслеживаемый `local.properties`. В Windows для `gradlew` использовать Git Bash: `gradlew.bat` в репозитории отсутствует.

```sh
# Git Bash / POSIX shell
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Требуется внешний `app/libs/sherpa-onnx.aar`: он игнорируется Git. Версия 1.13.1, URL и эталонный SHA-256 заданы в [скрипте установки](scripts/install-latest-debug.ps1) и [CI](.github/workflows/android.yml); сверять файл с ними.

```powershell
# Только после выполнения условий скрипта; подставить текущую ветку.
.\scripts\install-latest-debug.ps1 -Branch '<current-branch>' -RunTests -BuildOnly
# Для установки на единственное авторизованное устройство убрать -BuildOnly.
```

- CI собирает debug APK; на PR запускает `testDebugUnitTest`. Точные флаги — в `.github/workflows/android.yml`. Секреты подписи настраиваются вне Git; их значения не выводить.
- Найден один JUnit 4 тест: `app/src/test/java/app/slovami/ai/summary/ner/SlovnetDumpTest.kt`, сравнение с бинарными NER-эталонами из assets. Инструментальных/UI-тестов и настроенных ktlint/detekt не найдено.
- По `CONTRIBUTING.md` изменения UI проверять на физическом телефоне. Сценарии записи, импорта, расшифровки и DOCX, маленький экран и увеличенный шрифт перечислены в [docs/BUILDING.md](docs/BUILDING.md).

## Предлагаемый порядок работы Codex (новые рекомендации)

- Начинать с `git status --short` и проверки diff. Сохранять чужие незакоммиченные изменения; не выполнять автоматический stash/reset или переключение ветки ради сборочного скрипта.
- Ограничивать diff задачей. После изменения кода запускать сборку и релевантные тесты; в отчёте различать выполненные проверки и проверки, которые не удалось запустить. Для изменения только документации достаточно проверить ссылки и итоговый diff.
- Дополнительная проверка Android Lint: `./gradlew lintDebug` (стандартная задача AGP; в текущем CI не запускается). Не выдавать её за существующий обязательный gate.
- Если документы расходятся с кодом/скриптами, сообщать расхождение и сверять исполняемую реализацию; не исправлять посторонние проблемы без задачи.

## Известные ограничения анализа

- Старые инструкции о включённом AAR, загрузке APK скриптом из Actions и `gradlew.bat` заменены описанием текущего локального процесса. При изменении скрипта обновлять связанные инструкции.
- Проверять актуальное состояние сборки и тестов в каждой задаче; успешная сборка не заменяет проверку записи, ASR и экспорта на устройстве.
