package jakoboeu.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component

@JsonIgnoreProperties(ignoreUnknown = true)
class SiteOfBiologicalInterest(
    @field:JsonProperty("title") val title: String,
    @field:JsonProperty("site_name") val siteName: String,
    @field:JsonProperty("features") val features: String,
) {
    constructor() : this("", "", "")
}

@JsonIgnoreProperties(ignoreUnknown = true)
class OsGreenspace(
    @field:JsonProperty("title") val title: String,
    @field:JsonProperty("function") val siteType: String,
) {
    constructor() : this("", "")
}

val sitesOfBiologicalInterestSchema: CsvSchema = CsvSchema.builder()
    .addColumn("title")
    .addColumn("fid")
    .addColumn("site_id")
    .addColumn("site_name")
    .addColumn("cent_gr")
    .addColumn("site_gra")
    .addColumn("district")
    .addColumn("features")
    .addColumn("date_est")
    .build()

val osGreenspaceSchema: CsvSchema = CsvSchema.builder()
    .addColumn("title")
    .addColumn("id")
    .addColumn("function")
    .addColumn("distName1")
    .build()

@Component
class GreenspaceTypeLoader {

    val osSiteIndex: Map<String,String> by lazy {
        val osGreenspaces = csvMapper().readerFor(OsGreenspace::class.java)
            .with(osGreenspaceSchema.withSkipFirstDataRow(true))
            .readValues<OsGreenspace>(javaClass.classLoader.getResourceAsStream("os_greenspace_mapping.csv"))
            .readAll()

        osGreenspaces.associate { it.title to it.siteType }
    }

    val biologicalInterestSites: Map<String,String> by lazy {
        val sitesOfBiologicalInterest = csvMapper().readerFor(SiteOfBiologicalInterest::class.java)
            .with(sitesOfBiologicalInterestSchema.withSkipFirstDataRow(true))
            .readValues<SiteOfBiologicalInterest>(javaClass.classLoader.getResourceAsStream("greater_manchester_sites_of_biological_interest.csv"))
            .readAll()
        sitesOfBiologicalInterest.associate { it.title to it.features }
    }

    fun distinctOsGreenspaceTypes() : Set<String> = this.osSiteIndex.values.toSet()
    fun osGreenspaceType(title: String) : String? = this.osSiteIndex[title]

    fun distinctBiologicalSiteTypes() : Set<String> = this.biologicalInterestSites.values.toSet()
    fun biologicalSiteType(title: String) : String? = this.biologicalInterestSites[title]

    companion object {
        fun csvMapper(): CsvMapper {
            val csvMapper = CsvMapper().apply {
                enable(CsvParser.Feature.TRIM_SPACES)
                enable(CsvParser.Feature.SKIP_EMPTY_LINES)
            }
            return csvMapper
        }
    }
}