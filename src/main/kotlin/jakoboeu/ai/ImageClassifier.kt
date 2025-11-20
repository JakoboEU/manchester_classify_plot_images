package jakoboeu.ai

import com.fasterxml.jackson.databind.ObjectMapper
import jakoboeu.model.ImageVision
import jakoboeu.model.imageVisionSchemaJson
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.io.File

@Component
class ImageClassifier(
    private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper) {

    private val chat = ChatClient.create(this.chatModel);

    fun classifyImage(imagePath: File): ImageVision {
        val prompt = """
            You are analysing a single photograph of an urban outdoor space.

            You are given:
            - the image
            - a JSON Schema describing the expected output structure and field meanings
            
            Your task:
            - Produce ONE JSON object describing what is visible in the image.
            - The JSON MUST strictly conform to the provided JSON Schema, including:
              - property names
              - required properties
              - data types
              - enum values
              - and any constraints described in the schema (for example in \"description\" fields).
            
            Use only information that is clearly visible in the image.
            Do NOT guess the geographic location, city, country, or time of year.
            If something is uncertain, choose a conservative value that still respects the schema
            (e.g. lower confidence scores or \"uncertain\" options where available).
            
            Important:
            - Read and follow the \"description\" metadata in the schema to understand what each field means
              (for example, score ranges like 1–5 and how to interpret them).
            - Do NOT add extra properties that are not defined in the schema.
            - Output ONLY the JSON object, with no surrounding explanation.
            
            Schema:
            ${imageVisionSchemaJson()}
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