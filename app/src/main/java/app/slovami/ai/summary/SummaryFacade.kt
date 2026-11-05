package app.slovami.ai.summary

import app.slovami.ai.summary.default_tier.DefaultSummaryService
import app.slovami.ai.summary.default_tier.SummaryResult
import app.slovami.ai.summary.ner.NerDiagnostics
import app.slovami.ai.summary.ner.NerProvider
import app.slovami.ai.summary.ner.NoOpNerProvider
import app.slovami.ai.summary.ner.SlovnetNerProvider
import java.io.File

/** Offline extractive summarization using NER, LexRank and rules. */
class SummaryFacade(
    private val nerProvider: NerProvider = NoOpNerProvider(),
) {
    private val summaryService = DefaultSummaryService(nerProvider)

    fun summarize(transcript: String): SummaryResult = summaryService.summarize(transcript)

    companion object {
        /** When context is supplied, also run diagnostics against the bundled NER fixtures. */
        fun withSlovnet(
            navecDir: File,
            slovnetDir: File,
            context: android.content.Context? = null,
        ): SummaryFacade {
            val provider = SlovnetNerProvider.load(navecDir, slovnetDir)
            if (context != null) {
                try {
                    NerDiagnostics.run(context, provider.ner)
                } catch (t: Throwable) {
                    android.util.Log.w("SummaryFacade", "diagnostic run failed", t)
                }
            }
            return SummaryFacade(provider)
        }

        /** Fallback when NER models cannot be loaded. */
        fun withoutNer(): SummaryFacade = SummaryFacade(NoOpNerProvider())
    }
}
