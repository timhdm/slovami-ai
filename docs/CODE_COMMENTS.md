# Какие комментарии оставлены

Комментарии нужны разработчикам: они поясняют причины решений и ограничения,
которые трудно восстановить по названиям методов. Схемы расположения элементов
UI заменены просмотром интерфейса в Android Studio/Compose Preview.

Основные сохранённые пояснения:

| Где | Что объясняют |
| --- | --- |
| [MainActivity.kt](../app/src/main/java/app/slovami/ai/MainActivity.kt), [SlovamiAiApp.kt](../app/src/main/java/app/slovami/ai/SlovamiAiApp.kt) | Порядок установки splash screen, применение сохранённой локали до создания экрана. |
| [AsrService.kt](../app/src/main/java/app/slovami/ai/asr/AsrService.kt) | Ограничение длины входа GigaAM, сдвиг timestamps, эвристическая уверенность, пересоздание диаризатора вместо проблемного setConfig. |
| [Transcript.kt](../app/src/main/java/app/slovami/ai/asr/Transcript.kt), [EnglishAsrService.kt](../app/src/main/java/app/slovami/ai/asr/EnglishAsrService.kt) | Единицы времени, диапазоны сэмплов, смысл confidence и контракт Whisper. |
| [AudioImporter.kt](../app/src/main/java/app/slovami/ai/audio/AudioImporter.kt), [AudioRecorder.kt](../app/src/main/java/app/slovami/ai/audio/AudioRecorder.kt), [Mp4Encoder.kt](../app/src/main/java/app/slovami/ai/audio/Mp4Encoder.kt) | Формат PCM/WAV, состояние ресемплинга между блоками, завершение заголовка и освобождение медиаресурсов. |
| [LongAudioProcessor.kt](../app/src/main/java/app/slovami/ai/audio/LongAudioProcessor.kt) | Сдвиг времени при склейке и отсутствие сопоставления одного человека между частями записи. |
| [LongTaskRunner.kt](../app/src/main/java/app/slovami/ai/audio/LongTaskRunner.kt), [InterviewViewModel.kt](../app/src/main/java/app/slovami/ai/ui/InterviewViewModel.kt) | Application-scope, повторное подключение UI к результатам, порядок инициализации полей до init, сброс результатов предыдущей сессии. |
| [InterviewScreen.kt](../app/src/main/java/app/slovami/ai/ui/InterviewScreen.kt), [ParticipantsEditor.kt](../app/src/main/java/app/slovami/ai/ui/ParticipantsEditor.kt) | Доступ к URI после пересоздания UI, обход обрезания текста в clipboard, navigation insets, сохранение ID участников. |
| [AssetExtractor.kt](../app/src/main/java/app/slovami/ai/downloader/AssetExtractor.kt) | Почему встроенные и скачанные модели приводятся к файловым путям. |
| [ModelDownloader.kt](../app/src/main/java/app/slovami/ai/downloader/ModelDownloader.kt), [ModelStorage.kt](../app/src/main/java/app/slovami/ai/downloader/ModelStorage.kt), [DownloadService.kt](../app/src/main/java/app/slovami/ai/downloader/DownloadService.kt) | HTTP Range/200/206, сохранение .partial, размер и SHA-256, отмена загрузки и assets fallback. |
| [SessionStore.kt](../app/src/main/java/app/slovami/ai/data/SessionStore.kt), [DocxBuilder.kt](../app/src/main/java/app/slovami/ai/docx/DocxBuilder.kt) | Совместимость JSON сессий, блокирующий файловый ввод-вывод и структура OOXML. |
| [NavecEmbeddings.kt](../app/src/main/java/app/slovami/ai/summary/ner/NavecEmbeddings.kt) | Бинарный формат и little-endian, восстановление вектора, границы словаря; причина чтения индексов в heap вместо mmap. |
| [SlovnetNer.kt](../app/src/main/java/app/slovami/ai/summary/ner/SlovnetNer.kt) | Формы тензоров, раскладка весов, порядок Conv/ReLU/BatchNorm, сохранённый running_std, BIO/CRF-декодирование. Схема модели оставлена как описание численного контракта. |
| [SlovnetShapeClassifier.kt](../app/src/main/java/app/slovami/ai/summary/ner/SlovnetShapeClassifier.kt), [NerJunkFilter.kt](../app/src/main/java/app/slovami/ai/summary/ner/NerJunkFilter.kt) | Соответствие категориям модели и причины фильтрации ложных сущностей разговорной речи. |
| [PageRank.kt](../app/src/main/java/app/slovami/ai/summary/lexrank/PageRank.kt), [TfIdfVectorizer.kt](../app/src/main/java/app/slovami/ai/summary/lexrank/TfIdfVectorizer.kt), [LexRankSummarizer.kt](../app/src/main/java/app/slovami/ai/summary/lexrank/LexRankSummarizer.kt) | Разреженные векторы, нормализация, сходимость и порядок ранжирования предложений. |
| [SlovnetDumpTest.kt](../app/src/test/java/app/slovami/ai/summary/ner/SlovnetDumpTest.kt), [NerDiagnostics.kt](../app/src/main/java/app/slovami/ai/summary/ner/NerDiagnostics.kt) | Источники бинарных эталонов, размеры промежуточных результатов и расположение диагностического отчёта. |

Краткие KDoc с единицами измерения, смыслом полей, условиями вызова и возвратом
ошибок также оставлены рядом с объявлениями. Лицензионные уведомления,
пояснения упаковки моделей в Gradle и XML-ограничения Android сохранены.

Удалены устаревшая схема «MK-Интервью», описания ещё не существующих этапов
разработки, пересказ очевидных UI-элементов, декоративные разделители и
неподтверждённые оценки скорости. Исправлены старые имена каталогов экспорта,
описание хранения моделей и утверждения о жизненном цикле Android.
