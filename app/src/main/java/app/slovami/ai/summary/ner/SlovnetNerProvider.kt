package app.slovami.ai.summary.ner

import app.slovami.ai.summary.default_tier.NerEntity
import java.io.File

/**
 * Для каждого предложения выполняет Slovnet NER, суммирует упоминания PER/ORG/LOC
 * и применяет фильтр ложных сущностей.
 */
class SlovnetNerProvider(
    val ner: SlovnetNer,
    private val sentenceSegmenter: (String) -> List<String> =
        { text -> app.slovami.ai.summary.core.RuSentenceSegmenter.segment(text) },
) : NerProvider {

    override val description: String = "Slovnet NER (WordCNN + CRF, 28 MB models, offline)"

    override fun extract(text: String): NerProvider.Result {
        val sentences = sentenceSegmenter(text)
        val perCounts = HashMap<String, Int>()
        val orgCounts = HashMap<String, Int>()
        val locCounts = HashMap<String, Int>()

        for (sentence in sentences) {
            val tokens = tokenize(sentence)
            if (tokens.isEmpty()) continue
            val tags = ner.predict(tokens)
            val spans = ner.extractSpans(tokens, tags)
            for (span in spans) {
                val map = when (span.type) {
                    "PER" -> perCounts
                    "ORG" -> orgCounts
                    "LOC" -> locCounts
                    else -> continue
                }
                map.merge(span.text, 1, Int::plus)
            }
        }

        // Apply junk filter
        val perFiltered = NerJunkFilter.filter(perCounts, "PER")
        val orgFiltered = NerJunkFilter.filter(orgCounts, "ORG")
        val locFiltered = NerJunkFilter.filter(locCounts, "LOC")

        return NerProvider.Result(
            persons = toEntities(perFiltered, "PER"),
            organizations = toEntities(orgFiltered, "ORG"),
            locations = toEntities(locFiltered, "LOC"),
            providerDescription = description,
        )
    }

    /**
     * Простая токенизация: слова + одиночные знаки пунктуации.
     * Эквивалент `re.findall(r"\w+|[^\w\s]", text)` из Python-эталона.
     */
    private fun tokenize(text: String): List<String> {
        val out = mutableListOf<String>()
        val regex = Regex("\\w+|[^\\w\\s]", RegexOption.UNIX_LINES)
        for (m in regex.findAll(text)) out.add(m.value)
        return out
    }

    private fun toEntities(map: Map<String, Int>, type: String): List<NerEntity> {
        return map.entries
            .sortedByDescending { it.value }
            .map { (text, count) ->
                NerEntity(
                    type = type,
                    text = text,
                    normalized = text.lowercase(),
                    count = count,
                )
            }
    }

    companion object {
        /**
         * Удобный конструктор: создаёт провайдер из путей к распакованным
         * директориям navec и slovnet (оба содержат arrays/<i>.bin и vocabs/<name>.gz).
         */
        fun load(navecDir: File, slovnetDir: File): SlovnetNerProvider {
            val navec = NavecEmbeddings.load(navecDir)
            val slovnet = SlovnetNer.load(slovnetDir, navec)
            return SlovnetNerProvider(slovnet)
        }
    }
}
