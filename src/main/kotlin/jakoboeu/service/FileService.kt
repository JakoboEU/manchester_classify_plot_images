package jakoboeu.service

import jakoboeu.model.PlotImage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FileService(@Value("\${jakobo.input.dir}") private val inputDir: String,) {

    fun listAllPlotImageFiles(): List<PlotImage> {
        val d = java.io.File(inputDir)

        if (!d.exists()) {
            throw IllegalArgumentException("Directory $inputDir does not exist.")
        }

        return d.listFiles()
            .map { Pair(it, PlotImage.accept(it)) }
            .filter { it.second != null }
            .map { PlotImage.create(it.first, it.second!!) }
            .toList()
    }
}