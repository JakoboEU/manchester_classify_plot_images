package jakoboeu.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.springframework.stereotype.Component

@JsonIgnoreProperties(ignoreUnknown = true)
class SurveyData(
    @field:JsonProperty("title") val title: String,
    @field:JsonProperty("Land_use") val landUse: String,
    @field:JsonProperty("Other_land_use") val otherLandUse: String
) {
    constructor() : this("", "", "")
}

val surveyDataSchema: CsvSchema = CsvSchema.builder()
    .addColumn("ec5_uuid")
    .addColumn("created_at")
    .addColumn("uploaded_at")
    .addColumn("title")
    .addColumn("Survey_square_ID")
    .addColumn("Area_ID")
    .addColumn("Surveyor_name")
    .addColumn("Date")
    .addColumn("Start_time")
    .addColumn("Start_decibels")
    .addColumn("Area_photo")
    .addColumn("Birds_Songbirds")
    .addColumn("Birds_Warblers")
    .addColumn("Birds_Finches")
    .addColumn("Birds_Tits")
    .addColumn("Birds_Crows")
    .addColumn("Birds_Pigeons")
    .addColumn("Birds_Aerial")
    .addColumn("Birds_Other")
    .addColumn("Unused_audio")
    .addColumn("Insects_Butterflies")
    .addColumn("Insects_Moths")
    .addColumn("Insects_Bumblebees")
    .addColumn("Insects_Hoverflies")
    .addColumn("Insects_Other")
    .addColumn("Insect_photo")
    .addColumn("Plants_Trees")
    .addColumn("Plants_Midstory")
    .addColumn("Plants_Ferns")
    .addColumn("Plants_Orchids")
    .addColumn("Plants_Fungi")
    .addColumn("Plants_Micro")
    .addColumn("Plants_Grasses")
    .addColumn("Plants_Other")
    .addColumn("Plants_Other_Heather")
    .addColumn("Plant_photo")
    .addColumn("Lawn_present")
    .addColumn("Lawn_Area_m")
    .addColumn("Lawn_Shard_length_cm")
    .addColumn("Lawn_Other_species")
    .addColumn("Lawn_photo")
    .addColumn("Wild_grass_present")
    .addColumn("Wild_grass_area_m")
    .addColumn("Wild_Shard_length_cm")
    .addColumn("Wild_Other_species")
    .addColumn("Wild_photo")
    .addColumn("Rubbish_present")
    .addColumn("Rubbish_photo")
    .addColumn("Flower_beds")
    .addColumn("Flower_beds_photo")
    .addColumn("Natural_litter")
    .addColumn("Natural_litter_photo")
    .addColumn("Other_water_present")
    .addColumn("Water_photo")
    .addColumn("Land_use")
    .addColumn("Other_land_use")
    .addColumn("Pedestrian_count")
    .addColumn("End_decibels")
    .addColumn("End_time")
    .build();

@Component
class SurveyPlotDataLoader {

    private val totalGroundCoverPerPlot = 400;

    val siteIndex: Map<String, SurveyData> by lazy {
        val csvMapper = CsvMapper().apply {
            enable(CsvParser.Feature.TRIM_SPACES)
            enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        }

        csvMapper.readerFor(SurveyData::class.java)
            .with(surveyDataSchema.withSkipFirstDataRow(true))
            .readValues<SurveyData>(javaClass.classLoader.getResourceAsStream("form-1__survey.csv"))
            .readAll()
            .associateBy { it.title }
    }

    fun surveyPlotData(title: String) : SurveyData? = this.siteIndex[title]
}