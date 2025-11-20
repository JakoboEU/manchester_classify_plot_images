package jakoboeu.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Component
class ImageResizer(@param:Value("\${jakobo.working.dir}") val workingDirectory: String,) {
    private val maxWidth : Int = 800
    private val maxHeight : Int = 600
    private val resizedImageDir : File = File(File(workingDirectory), "resizedImages")
    val logger: Logger = LoggerFactory.getLogger(ImageResizer::class.java)

    fun resizeImage(plotImage: File) : File {
        logger.info("Working directory: $resizedImageDir")

        if (!resizedImageDir.exists()) {
            resizedImageDir.mkdirs();
        }

        val outputImage = File(resizedImageDir, plotImage.name)

        if (!outputImage.exists()) {
            resizeImageFile(
                inputFile = plotImage,
                outputFile = outputImage
            )
        }

        return outputImage
    }

    private fun resizeImageFile(
        inputFile: File,
        outputFile: File
    ) : File {
        require(inputFile.exists()) { "Input file not found: ${inputFile.absolutePath}" }

        val original = ImageIO.read(inputFile)
            ?: error("Unsupported or corrupt image: ${inputFile.name}")

        val (newW, newH) = fitWithin(original.width, original.height, maxWidth, maxHeight)
        val resized = resizeBuffered(original, newW, newH)

        ImageIO.write(resized, "jpg", outputFile)
        return outputFile
    }

    private fun fitWithin(w: Int, h: Int, maxW: Int, maxH: Int): Pair<Int, Int> {
        val scale = minOf(maxW.toDouble() / w, maxH.toDouble() / h).coerceAtMost(1.0)
        return (w * scale).toInt().coerceAtLeast(1) to (h * scale).toInt().coerceAtLeast(1)
    }

    private fun resizeBuffered(src: BufferedImage, newW: Int, newH: Int): BufferedImage {
        val type = if (src.type == 0) BufferedImage.TYPE_INT_ARGB else src.type
        val dst = BufferedImage(newW, newH, type)
        val g2: Graphics2D = dst.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.drawImage(src, 0, 0, newW, newH, null)
        g2.dispose()
        return dst
    }
}