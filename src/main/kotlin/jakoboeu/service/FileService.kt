package jakoboeu.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakoboeu.model.ImageVisionWithIdentity
import jakoboeu.model.PlotImage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path

@Component
class FileService(
    @param:Value("\${jakobo.input.dir}") private val inputDir: String,
    @param:Value("\${jakobo.output.file}") private val outputFile: String,
    private val objectMapper: ObjectMapper,
    ) {

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

    fun save(output: List<ImageVisionWithIdentity>) {
        val file = java.io.File(outputFile)

        Files.newBufferedWriter(
            file.toPath(),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { writer ->
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, output)
        }
    }
}