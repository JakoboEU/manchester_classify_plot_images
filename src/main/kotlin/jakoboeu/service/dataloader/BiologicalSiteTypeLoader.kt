package jakoboeu.service.dataloader

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

@Component
class BiologicalSiteTypeLoader : SiteTypeLoader {
    val biologicalInterestSites: Map<String,String> by lazy {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
            enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        }

        val sitesOfBiologicalInterest = csvMapper.readerFor(SiteOfBiologicalInterest::class.java)
            .with(sitesOfBiologicalInterestSchema.withSkipFirstDataRow(true))
            .readValues<SiteOfBiologicalInterest>(javaClass.classLoader.getResourceAsStream("greater_manchester_sites_of_biological_interest.csv"))
            .readAll()
        sitesOfBiologicalInterest.associate { it.title to it.features }
    }

    override fun distinctSiteTypes() : Set<String> = this.biologicalInterestSites.values.toSet()
    override fun siteType(title: String) : String? = this.biologicalInterestSites[title]

}