package app.slovami.ai.downloader

/**
 * Каталог файлов Hugging Face с ожидаемыми размерами и SHA-256.
 * Проверка размещённых моделей и создание полного манифеста: tools/mirror-models.py.
 */
object ModelRegistry {

    /**
     * Hugging Face организация и репозиторий где лежат модели.
     * URL: https://huggingface.co/TimHdm/slovami-ai-models
     */
    const val HF_REPO = "TimHdm/slovami-ai-models"

    /**
     * Branch/revision в репо с моделями. На HF обычно "main".
     * При обновлении моделей проще всего будет создать branch "v2" и поменять здесь.
     */
    const val MODELS_REVISION = "main"

    /**
     * Базовый URL для всех моделей. Каждая модель добавляет свой filename.
     * Hugging Face использует /resolve/{revision}/ для прямого доступа к файлам.
     */
    val BASE_URL: String
        get() = "https://huggingface.co/$HF_REPO/resolve/$MODELS_REVISION/"

    /**
     * Группа модели — определяет когда скачивается.
     */
    enum class Tier {
        /** Без неё приложение не работает (ASR + tokens) */
        REQUIRED,
        /** Скачивается по умолчанию вместе с REQUIRED */
        DEFAULT_EMBEDDING,
        /** Скачивается опционально, по запросу пользователя */
        OPTIONAL_EMBEDDING,
        /** Английская ASR модель — опциональная, качается при выборе EN */
        OPTIONAL_EN_ASR,
    }

    /**
     * Один скачиваемый ресурс.
     */
    data class Model(
        val id: String,
        val filename: String,
        val sizeBytes: Long,
        val sha256: String,
        val tier: Tier,
        /**
         * Подкаталог в filesDir/models/.
         * null означает прямо в filesDir/models/.
         */
        val subdir: String? = null,
    ) {
        /**
         * Относительный путь внутри HF репо (после BASE_URL).
         * Если subdir задан — возвращает "{subdir}/{filename}", иначе только "{filename}".
         * Использует прямой слэш (для URL), а не File.separator.
         */
        val urlSuffix: String get() = if (subdir != null) "$subdir/$filename" else filename
    }


    /** ASR — GigaAM-v3 e2e_ctc int8. Главная модель распознавания. */
    val ASR_MAIN = Model(
        id = "asr_gigaam_v3",
        filename = "gigaam_v3_e2e_ctc_int8.onnx",
        sizeBytes = 319_869_121L,  // точный размер ~305 MB
        sha256 = "0aacb41f70f0f5aaac4b45dd430337b9e16b180f22c72af04db8516e7609c3c0",
        tier = Tier.REQUIRED,
    )

    /** Speaker embedding CAM++. Default по балансу скорости и качества. */
    val EMBEDDING_CAMPLUS = Model(
        id = "embedding_camplus",
        filename = "speaker_embedding_camplus.onnx",
        sizeBytes = 28_281_138L,   // ~27 MB
        sha256 = "f682b514c05d947ee3fa91cd6ec6c5c7543479a128373fa29b1faedccd21fd11",
        tier = Tier.DEFAULT_EMBEDDING,
    )

    /** Speaker embedding ERes2Net V1. Эталон качества, медленный. */
    val EMBEDDING_V1 = Model(
        id = "embedding_v1",
        filename = "speaker_embedding.onnx",
        sizeBytes = 116_058_710L,  // ~111 MB
        sha256 = "19547e85b6c14ec44b8add4e7cb9ce353c7e995d4f1c9ffd408176ac3a2d6895",
        tier = Tier.OPTIONAL_EMBEDDING,
    )

    /** Speaker embedding ERes2Net V2. */
    val EMBEDDING_V2 = Model(
        id = "embedding_v2",
        filename = "speaker_embedding_v2.onnx",
        sizeBytes = 71_441_526L,   // ~68 MB
        sha256 = "bf1a75b9930474cf3389ef415e6e5d38ca96fea4a3a00f7e301d080a58ee2239",
        tier = Tier.OPTIONAL_EMBEDDING,
    )

    // Whisper хранится в en/ и входит в first-run набор при выборе EN.

    /** Английский ASR encoder (int8). */
    val ASR_EN_ENCODER = Model(
        id = "asr_en_encoder",
        filename = "whisper_base_en_encoder_int8.onnx",
        sizeBytes = 29_120_534L,   // ~28 MB
        sha256 = "ef6b936f4c9b1d90a3b68634b60c4ed8576b26172b33c2535ec0e933c9edb823",
        tier = Tier.OPTIONAL_EN_ASR,
        subdir = "en",
    )

    /** Английский ASR decoder (int8). */
    val ASR_EN_DECODER = Model(
        id = "asr_en_decoder",
        filename = "whisper_base_en_decoder_int8.onnx",
        sizeBytes = 130_669_978L,  // ~125 MB
        sha256 = "f7162ad6db2dbef16cfaeaa7f945b9d7dd9c1b8d472f6aca82f2273d185e4d41",
        tier = Tier.OPTIONAL_EN_ASR,
        subdir = "en",
    )

    /** Английские токены. */
    val ASR_EN_TOKENS = Model(
        id = "asr_en_tokens",
        filename = "whisper_base_en_tokens.txt",
        sizeBytes = 835_554L,       // ~0.8 MB
        sha256 = "306cd27f03c1a714eca7108e03d66b7dc042abe8c258b44c199a7ed9838dd930",
        tier = Tier.OPTIONAL_EN_ASR,
        subdir = "en",
    )

    // Navec и Slovnet загружаются вместе из summary/; одного архива недостаточно для NER.

    /** Navec quantized embeddings (250K Russian words, 300d, PQ-100). */
    val NAVEC_NEWS = Model(
        id = "navec_news",
        filename = "navec_news.tar",
        sizeBytes = 26_634_240L,   // ~25 MB
        sha256 = "f07270833d78523edc5781538d67038e95b43975e4a7ae757c693b687f9cbfca",
        tier = Tier.DEFAULT_EMBEDDING,
        subdir = "summary",
    )

    /** Slovnet NER (WordCNN + CRF) для русского PER/ORG/LOC. */
    val SLOVNET_NER = Model(
        id = "slovnet_ner",
        filename = "slovnet_ner.tar",
        sizeBytes = 2_385_920L,    // ~2.3 MB
        sha256 = "16c3343a6572ddf0e2b2fc1923113de15a7f5ca5bde90e780dd590418665603e",
        tier = Tier.DEFAULT_EMBEDDING,
        subdir = "summary",
    )

    /** Модели summarization Default tier (обе нужны вместе). */
    val SUMMARY_BUNDLE: List<Model> = listOf(NAVEC_NEWS, SLOVNET_NER)

    /** Все английские модели вместе — полный комплект для EN распознавания. */
    val EN_ASR_BUNDLE: List<Model> = listOf(ASR_EN_ENCODER, ASR_EN_DECODER, ASR_EN_TOKENS)

    /** Все модели в реестре. */
    val ALL: List<Model> = listOf(
        ASR_MAIN,
        EMBEDDING_CAMPLUS,
        EMBEDDING_V1,
        EMBEDDING_V2,
        ASR_EN_ENCODER,
        ASR_EN_DECODER,
        ASR_EN_TOKENS,
        NAVEC_NEWS,
        SLOVNET_NER,
    )

    /** Модели для first-run скачивания (REQUIRED + DEFAULT_EMBEDDING) ~332 MB.
     *  Прежняя статическая версия — для RU сценария. Оставлена для обратной совместимости. */
    val FIRST_RUN_REQUIRED: List<Model> = ALL.filter {
        it.tier == Tier.REQUIRED || it.tier == Tier.DEFAULT_EMBEDDING
    }

    /**
     * Модели для first-run скачивания с учётом выбранного языка:
     *   - "RU" (или null/пустое) → GigaAM-v3 + camplus = ~332 MB
     *   - "EN"                     → Whisper base.en + camplus = ~180 MB
     *
     * Диаризация (camplus) нужна для обоих языков, поэтому входит в оба набора.
     */
    fun firstRunModelsFor(language: String?): List<Model> = when (language) {
        "EN" -> EN_ASR_BUNDLE + EMBEDDING_CAMPLUS
        else -> listOf(ASR_MAIN, EMBEDDING_CAMPLUS)   // RU или default
    }

    /** Сумма байт для first-run с учётом языка. */
    fun firstRunTotalBytesFor(language: String?): Long =
        firstRunModelsFor(language).sumOf { it.sizeBytes }

}
