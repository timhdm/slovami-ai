package app.slovami.ai.summary.ner

import app.slovami.ai.summary.default_tier.NerEntity

/**
 * Интерфейс для NER-провайдера.
 *
 * [SlovnetNerProvider] выполняет инференс; [NoOpNerProvider] позволяет
 * сформировать резюме без сущностей, если модели NER недоступны.
 */
interface NerProvider {

    /**
     * Описание провайдера (попадает в шапку markdown).
     */
    val description: String

    /**
     * Извлечь сущности из текста.
     */
    fun extract(text: String): Result

    data class Result(
        val persons: List<NerEntity>,
        val organizations: List<NerEntity>,
        val locations: List<NerEntity>,
        val providerDescription: String,
    )
}

/**
 * Fallback без NER: возвращает пустые списки сущностей.
 */
class NoOpNerProvider : NerProvider {
    override val description: String =
        "LexRank + правила (без NER)"

    override fun extract(text: String): NerProvider.Result =
        NerProvider.Result(
            persons = emptyList(),
            organizations = emptyList(),
            locations = emptyList(),
            providerDescription = description,
        )
}
