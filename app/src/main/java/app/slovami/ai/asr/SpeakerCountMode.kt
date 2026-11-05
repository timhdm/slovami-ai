package app.slovami.ai.asr

/**
 * Single пропускает диаризацию; Fixed задаёт число кластеров;
 * Auto передаёт numClusters = -1 для автоопределения.
 */
sealed class SpeakerCountMode {
    data object Single : SpeakerCountMode()
    data class Fixed(val count: Int) : SpeakerCountMode() {
        init { require(count in 2..4) { "Fixed count must be 2..4, got $count" } }
    }
    data object Auto : SpeakerCountMode()

    /** Короткая ASCII-метка для UI. Локализованная метка формируется в UI-слое
     *  (InterviewScreen.kt) через stringResource — сама модель не зависит от Context. */
    val label: String
        get() = when (this) {
            Single   -> "1"
            is Fixed -> count.toString()
            Auto     -> "AUTO"
        }

    companion object {
        /** Все доступные варианты в порядке для UI: 1, 2, 3, 4, Авто.
         *  Get-функция, не val — иначе при инициализации companion object
         *  ссылки на data object Single/Auto могут быть ещё null. */
        val all: List<SpeakerCountMode>
            get() = listOf(
                Single,
                Fixed(2),
                Fixed(3),
                Fixed(4),
                Auto,
            )
    }
}
