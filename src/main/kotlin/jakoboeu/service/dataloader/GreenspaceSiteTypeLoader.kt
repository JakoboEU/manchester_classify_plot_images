package jakoboeu.service.dataloader

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component

@JsonIgnoreProperties(ignoreUnknown = true)
class OsGreenspace(
    @field:JsonProperty("title") val title: String,
    @field:JsonProperty("function") val siteType: String,
) {
    constructor() : this("", "")
}

val osGreenspaceSchema: CsvSchema = CsvSchema.builder()
    .addColumn("title")
    .addColumn("id")
    .addColumn("function")
    .addColumn("distName1")
    .build()

@Component
class GreenspaceSiteTypeLoader : SiteTypeLoader {

    val osSiteIndex: Map<String,String> by lazy {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
            enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        }

        val osGreenspaces = csvMapper.readerFor(OsGreenspace::class.java)
            .with(osGreenspaceSchema.withSkipFirstDataRow(true))
            .readValues<OsGreenspace>(javaClass.classLoader.getResourceAsStream("os_greenspace_mapping.csv"))
            .readAll()

        osGreenspaces.associate { it.title to it.siteType }
    }

    override fun distinctSiteTypes() : Set<String> = this.osSiteIndex.values.toSet()
    override fun siteType(title: String) : String? = this.osSiteIndex[title]
}