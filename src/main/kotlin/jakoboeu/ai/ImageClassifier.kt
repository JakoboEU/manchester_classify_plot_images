package jakoboeu.ai

import com.fasterxml.jackson.databind.ObjectMapper
import jakoboeu.model.ImageVision
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class ImageClassifier(
    private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper) {

    private val chat = ChatClient.create(this.chatModel);

    fun File.withExtension(newExt: String): File {
        val clean = newExt.removePrefix(".")
        val base = nameWithoutExtension
        val renamed = if (clean.isEmpty()) base else "$base.$clean"
        return parentFile?.resolve(renamed) ?: File(renamed)
    }

    fun cachedClassifyImage(imagePath: File): ImageVision {
        val cachedResponseLocation = imagePath.withExtension("json")

        fun readCachedResponse(): ImageVision =
            Files.newBufferedReader(cachedResponseLocation.toPath(), StandardCharsets.UTF_8).use { reader ->
                return objectMapper.readValue(reader, ImageVision::class.java)
            }

        fun classifyAndCacheResponse(): ImageVision {
            val result = classifyImage(imagePath)
            Files.newBufferedWriter(
                cachedResponseLocation.toPath(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            ).use { writer ->
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, result)
            }
            return result;
        }

        return if (cachedResponseLocation.exists()) {
            return readCachedResponse()
        } else {
            classifyAndCacheResponse()
        }
    }

    fun classifyImage(imagePath: File): ImageVision {
        val prompt = """
            You are a vision assistant. 
            Attached is an image of urban open space.  
            1) Classify the type of open space into a single word category.
            
            Only return the following JSON and replace:
            1) [openspace category] by the single word category
            2) Replace [image description] with a single sentence describing the openspace in the image, a second sentence describing the natural habitat, and a third sentence describing any objects in the image.
            3) Replace [llm model and version] with a short description of the LLM model used for this classification, along with any version or build information you can provide about yourself
            {
                "openspaceType": "[openspace category]",
                "imageDescription": "[image description]",
                "llmModelAndVersion": "[llm model and version]"
            }
        """.trimIndent()

        val json = chat.prompt()
            .options(
                OpenAiChatOptions.builder()
                .model("gpt-4.1-mini")
                .temperature(0.0)
                .build())
            .user { u ->
                u.text(prompt)
                    .media(MediaType.IMAGE_JPEG, FileSystemResource(imagePath))
            }
            .call()
            .content()

        val strippedJson = json!!.substring(json.indexOf('{'))
        return objectMapper.readValue(strippedJson, ImageVision::class.java)
    }
}