package app.slovami.ai.summary.default_tier

import app.slovami.ai.summary.extraction.NumberExtractor
import app.slovami.ai.summary.extraction.RiskKeywordMatcher
import app.slovami.ai.summary.lexrank.LexRankSummarizer

/**
 * Сущность, извлечённая NER.
 */
data class NerEntity(
    val type: String,    // "PER" | "ORG" | "LOC"
    val text: String,    // как написано в тексте
    val normalized: String, // лемматизированная форма
    val count: Int,
)

/**
 * Полный результат саммаризации.
 */
data class SummaryResult(
    /** Дата генерации. */
    val generatedAtMs: Long,
    /** Время обработки в мс. */
    val processingTimeMs: Long,

    // Статистика по входу
    val transcriptWords: Int,
    val transcriptChars: Int,
    val sentencesCount: Int,

    // Извлечённые сущности (NER)
    val persons: List<NerEntity>,
    val organizations: List<NerEntity>,
    val locations: List<NerEntity>,

    // Топ-цитаты по LexRank score
    val topQuotes: List<LexRankSummarizer.ScoredSentence>,

    // Top-K фрагментов в порядке появления (для секции "Ключевые фрагменты")
    val keyFragments: List<LexRankSummarizer.ScoredSentence>,

    // Риски с триггерами
    val risks: List<RiskKeywordMatcher.RiskMatch>,

    // Цифры с контекстом
    val numbers: List<NumberExtractor.NumberWithContext>,

    // Текстовое представление (готовый markdown для отображения)
    val markdown: String,

)
