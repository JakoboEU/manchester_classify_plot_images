package jakoboeu.ai

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakoboeu.model.GreenspaceType
import jakoboeu.service.SiteTypeMapper
import jakoboeu.service.SiteTypeSource
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component
import kotlin.text.indexOf

data class ReclassifiedSiteType(
    @param:JsonProperty("final_category")
    @param:JsonPropertyDescription("true if any buildings or built structures such as walls or fences are visible.")
    val finalCategory: GreenspaceType,
    @param:JsonProperty("confidence_1_5")
    @param:JsonPropertyDescription("Score 1 to 5 indicating certainty of the greenspace type classification: 1 = very uncertain, 5 = very certain.")
    val confidence: Int,
    @param:JsonProperty("agreement_pattern")
    @param:JsonPropertyDescription("a short code describing agreement (e.g. all_agree, field_os_agree_vision_differs, all_disagree)")
    val agreementPattern: String
)

fun reclassifiedSiteTypeSchemaJson(): String {
    val mapper = jacksonObjectMapper()
        .registerModule(
            KotlinModule.Builder().build()
        )

    val visitor = SchemaFactoryWrapper()
    mapper.acceptJsonFormatVisitor(ReclassifiedSiteType::class.java, visitor)
    val schema: JsonSchema = visitor.finalSchema()
    schema.id = "urn:reclassified-site-type:v1"
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
}

@Component
class GreenspaceReclassifier(
    private val chatModel: ChatModel,
    private val objectMapper: ObjectMapper,
    private val siteTypeMappers: List<SiteTypeMapper>) {

    private val chat = ChatClient.create(this.chatModel);

    fun reclassify(title: String, llmSiteType: GreenspaceType, llmSiteTypeCertainty1to5 : Int): ReclassifiedSiteType {
        val siteTypes = siteTypeMappers.map {
            it.mappedSiteType(title)
        }.filter { it != null }

        val prompt = """
            You are reconciling three land-use / greenspace labels into a single canonical category.

            You are given:
            - A fixed canonical greenspace taxonomy and definitions.
            - One or more independent source labels for one plot:
              - An classification assigned by an LLM visual assistant based on an image of the site [llm mapped]
              - A classification based on a field visit to the site [field mapped]
              - A classification based on the OS Greenspace GIS data set [os mappped]
              - A classification based on a biological site type data set [bio mapped]
            
            Your task:
            1. Compare the three mapped canonical labels.
            2. Apply the following decision logic:
               - If at least two sources agree on the same canonical label, choose that label.
               - If all three disagree, use the following priority:
                   [field_mapped] > [os_mapped] > [bio mapped] > [llm mapped],
                 but you may override this if two labels are semantically very similar and the third is a clear outlier,
                 or if the LLM vision classification has a high certainty (4 or 5 out of 5).
            3.  The JSON MUST strictly conform to the provided JSON Schema, including:
               - property names
               - required properties
               - data types
               - enum values
               - and any constraints described in the schema (for example in \"description\" fields).
            
            Important:
            - Use ONLY the canonical labels listed.
            - Do NOT invent new labels.
            - Output ONLY the JSON object, with no additional text.
            
            Schema:
            ${reclassifiedSiteTypeSchemaJson()}
            
            Labels available:
            [${GreenspaceType.entries.joinToString(", ") { "\"${it.value}\"" }}].
            
            Labels applied:
            - ${SiteTypeSource.LLM_VISION_CLASSIFICATION.description}: ${llmSiteType.value} (certainty: ${llmSiteTypeCertainty1to5}/5)
            ${siteTypes.joinToString("\n") { "- ${it!!.source.description}: ${it.siteType}" }}
        """.trimIndent()

        val json = chat.prompt()
            .options(
                OpenAiChatOptions.builder()
                    .model("gpt-4.1-mini")
                    .temperature(0.0)
                    .build())
            .user { u ->
                u.text(prompt)
            }
            .call()
            .content()

        val strippedJson = json!!.substring(json.indexOf('{'))
        return objectMapper.readValue(strippedJson, ReclassifiedSiteType::class.java)
    }
}