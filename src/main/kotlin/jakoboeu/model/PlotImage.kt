package jakoboeu.model

class PlotImage private constructor(
    val file: java.io.File,
    val title: String) {

    companion object CompanionFactory {
        private val PLOT_IMAGE_FILENAME_PATTERN = Regex("([0-9]{3,4})_(1?[0-9]).jpg")

        fun accept(file: java.io.File) = PLOT_IMAGE_FILENAME_PATTERN.matchEntire(file.name)

        fun create(file: java.io.File, matchResult: MatchResult) = PlotImage(file, matchResult.groups[1]!!.value + " " + matchResult.groups[2]!!.value)

        fun create(title: String, file: java.io.File) = PlotImage(file, title)
    }

    override fun toString(): String = title
}

