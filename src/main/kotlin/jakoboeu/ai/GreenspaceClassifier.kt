package jakoboeu.ai

import com.fasterxml.jackson.databind.ObjectMapper
import jakoboeu.model.GreenspaceType
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class GreenspaceClassifier(
    private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper) {

    private val chat = ChatClient.create(this.chatModel)

    fun reclassifyGreenspace(greenspaceTypes:Set<String>): Map<String,String> {
        val prompt = """
            Reclassify the following greenspace types:
             [${greenspaceTypes.joinToString(", ") { "\"$it\"" }}]
            into these categories:
             [${ GreenspaceType.entries.joinToString(", ") { "\"${it.value}\"" } }].
            Provide the result as a JSON object: 
            {
              "[original type]": "[reclassified type]",
              ..
            }
        """.trimIndent()

        val json = chat.prompt()
            .options(
                OpenAiChatOptions.builder()
                    .model("gpt-4.1-mini")
                    .temperature(0.0)
                    .build())
            .user { u -> u.text(prompt) }
            .call()
            .content()

        val strippedJson = json!!.substring(json.indexOf('{'))
        val result : Map<String,String> = objectMapper.readValue(strippedJson, Map::class.java) as Map<String, String>

        require(result.size == greenspaceTypes.size) {
            "Reclassification result size ${result.size} does not match input size ${greenspaceTypes.size}"
        }

        return result;
    }
}