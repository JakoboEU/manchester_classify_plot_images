package jakoboeu.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper
import com.fasterxml.jackson.module.jsonSchema.factories.VisitorContext
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

enum class DominantVegetationType(@JsonValue val value: String) {
    NONE_OR_PAVED("none_or_paved"),
    SHORT_MOWN_GRASS("short_mown_grass"),
    TALL_HERB_MEADOW("tall_herb_meadow"),
    SHRUB_DOMINATED("shrub_dominated"),
    TREE_DOMINATED_OPEN_UNDERSTOREY("tree_dominated_open_understorey"),
    CLOSED_WOODLAND("closed_woodland"),
    WATER_DOMINATED("water_dominated"),
    MIXED("mixed")
}

enum class PathType(@JsonValue val value: String) {
    NONE("none"),
    INFORMAL_EARTH_OR_MUD("informal_earth_or_mud"),
    MOWN_GRASS_TRACK("mown_grass_track"),
    GRAVEL("gravel"),
    PAVED_FOOTPATH("paved_footpath"),
    SHARED_USE_CYCLE_PATH("shared_use_cycle_path"),
    ROAD_FOR_MOTOR_VEHICLES("road_for_motor_vehicles")
}

enum class PathWidthCategory(@JsonValue val value: String) {
    NONE("none"),
    NARROW_UNDER_1M("narrow_under_1m"),
    MEDIUM_1_3M("medium_1_3m"),
    WIDE_OVER_3M("wide_over_3m")
}

enum class ApparentPublicAccess(@JsonValue val value: String) {
    CLEARLY_PUBLIC("clearly_public"),
    UNCERTAIN("uncertain"),
    LIKELY_PRIVATE_OR_GARDEN("likely_private_or_garden")
}

enum class VisibilityDistanceCategory(@JsonValue val value: String) {
    SHORT_LESS_10M("short_less_10m"),
    MEDIUM_10_30M("medium_10_30m"),
    LONG_OVER_30M("long_over_30m")
}

enum class MaintenanceIntensity(@JsonValue val value: String) {
    VERY_LOW_WILD("very_low_wild"),
    LOW_SEMI_WILD("low_semi_wild"),
    MODERATE("moderate"),
    HIGH_FORMAL("high_formal")
}

enum class MowingEvidence(@JsonValue val value: String) {
    NONE("none"),
    RECENT_MOW("recent_mow"),
    REGULARLY_MOWN_LAWN("regularly_mown_lawn")
}

data class VegetationStructure(
    @param:JsonProperty("dominant_vegetation_type")
    @param:JsonPropertyDescription("The main type of ground/vegetation cover visible in the scene. Example: “short_mown_grass” if it is mostly closely mown lawn; “tall_herb_meadow” if long grass/forbs dominate; “shrub_dominated” if shrubs dominate; “closed_woodland” if dense trees with limited visibility; “none_or_paved” if mostly hard surfaces.")
    val dominantVegetationType: DominantVegetationType,
)

data class AdjacentInfrastructure(
    @param:JsonProperty("buildings_visible")
    @param:JsonPropertyDescription("true if any buildings or built structures such as walls or fences are visible.")
    val buildingsVisible: Boolean,

    @param:JsonProperty("road_or_parking_visible")
    @param:JsonPropertyDescription("true if any road, driveway, or parking area is visible.")
    val roadOrParkingVisible: Boolean,

    @param:JsonProperty("railway_infrastructure_visible")
    @param:JsonPropertyDescription("true if any railway infrastructure such as tracks, platforms, or overhead wires is visible.")
    val railwayInfrastructureVisible: Boolean,

    @param:JsonProperty("water_body_visible")
    @param:JsonPropertyDescription("true if any water body such as a pond, lake, river, or canal is visible.")
    val waterBodyVisible: Boolean,

    @param:JsonProperty("play_equipment_present")
    @param:JsonPropertyDescription("true if play equipment such as swings/slides/climbing frames is visible.")
    val playEquipmentPresent: Boolean,
)

data class AccessibilityAndExperience(
    @param:JsonProperty("path_type")
    @param:JsonPropertyDescription("Best match for the main visible route a person could walk: none, informal_earth_or_mud, mown_grass_track, gravel, paved_footpath, shared_use_cycle_path, road_for_motor_vehicles.")
    val pathType: PathType,

    @param:JsonProperty("path_width_category")
    @param:JsonPropertyDescription("Width of the main visible path: none, narrow_under_1m, medium_1_3m, wide_over_3m.")
    val pathWidthCategory: PathWidthCategory,

    @param:JsonProperty("open_space_presence")
    @param:JsonPropertyDescription("true if there is a reasonably open area where a person could stand/walk/sit (not fully blocked by dense vegetation or obstacles).")
    val openSpacePresence: Boolean,

    @param:JsonProperty("visibility_distance_category")
    @param:JsonPropertyDescription("Typical visibility distance: short_less_10m, medium_10_30m, or long_over_30m.")
    val visibilityDistanceCategory: VisibilityDistanceCategory,
)

data class ClutterAndCondition(
    @param:JsonProperty("visual_clutter_score_1_5")
    @param:JsonPropertyDescription("1 = very simple/clean scene; 5 = very busy scene with many overlapping elements or occlusion. Use integers 1–5.")
    val visualClutterScore1To5: Int,

    @param:JsonProperty("litter_or_dumping_visible")
    @param:JsonPropertyDescription("true if rubbish or dumped items are clearly visible.")
    val litterOrDumpingVisible: Boolean,

    @param:JsonProperty("parked_cars_prominent")
    @param:JsonPropertyDescription("true if parked cars take up a noticeable part of the scene (not just tiny in the background).")
    val parkedCarsProminent: Boolean,

    @param:JsonProperty("street_furniture_visible")
    @param:JsonPropertyDescription("true if street furniture such as signs, lighting, or fencing is visible.")
    val streetFurnitureVisible: Boolean,

    @param:JsonProperty("number_of_people_visible")
    @param:JsonPropertyDescription("Approximate count of distinct people visible in the image. Use an integer 0 or greater.")
    val numberOfPeopleVisible: Int,

    @param:JsonProperty("maintenance_intensity")
    @param:JsonPropertyDescription("Perceived intensity of maintenance: very_low_wild, low_semi_wild, moderate, or high_formal.")
    val maintenanceIntensity: MaintenanceIntensity,

    @param:JsonProperty("mowing_evidence")
    @param:JsonPropertyDescription("Evidence of mowing on grassy areas: none, recent_mow, or regularly_mown_lawn.")
    val mowingEvidence: MowingEvidence
)


data class ImageContents(
    @param:JsonProperty("vegetation_structure")
    val vegetationStructure: VegetationStructure,

    @param:JsonProperty("accessibility_and_experience")
    val accessibilityAndExperience: AccessibilityAndExperience,

    @param:JsonProperty("clutter_and_condition")
    val clutterAndCondition: ClutterAndCondition,

    @param:JsonProperty("adjacent_infrastructure")
    val adjacentInfrastructure: AdjacentInfrastructure,

    @param:JsonProperty("photographic_clarity_score_1_5")
    @param:JsonPropertyDescription("1 = very poor quality (blurred, very dark/overexposed); 5 = very clear, well lit, easy to interpret. Use integers 1–5.")
    val photographicClarityScore1To5: Int,

    @param:JsonProperty("short_notes")
    val shortNotes: String
)

data class ImageVision(
    @param:JsonProperty("image")
    val imageContents: ImageContents,

    @param:JsonProperty("apparent_greenspace_type")
    val apparentGreenspaceType: GreenspaceType,

    @param:JsonProperty("apparent_greenspace_type_certainty_1_5")
    @param:JsonPropertyDescription("Score 1 to 5 indicating certainty of the greenspace type classification: 1 = very uncertain, 5 = very certain.")
    val apparentGreenspaceTypeCertainty1to5: Int,
)

data class ImageVisionWithIdentity(
    val title: String,
    val imageContents: ImageContents,
    val apparentGreenspaceType: GreenspaceType,
    val apparentGreenspaceTypeCertainty1to5: Int,
)

class ImageVisionVisitorContext : VisitorContext() {
    override fun javaTypeToUrn(jt: JavaType): String {
        // You can choose any scheme / prefix you like here
        val simpleName = jt.rawClass.simpleName
        return "plot-image-vision:$simpleName:v1"
    }
}

fun imageVisionSchemaJson(): String {
    val mapper = jacksonObjectMapper()
        .registerModule(
            KotlinModule.Builder().build()
        )

    val visitor = SchemaFactoryWrapper()
    visitor.setVisitorContext(ImageVisionVisitorContext())
    mapper.acceptJsonFormatVisitor(ImageVision::class.java, visitor)
    val schema: JsonSchema = visitor.finalSchema()

    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
}
