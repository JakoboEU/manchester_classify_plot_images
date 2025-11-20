package jakoboeu.service

import jakoboeu.ai.GreenspaceClassifier
import jakoboeu.service.dataloader.BiologicalSiteTypeLoader
import jakoboeu.service.dataloader.SiteTypeLoader
import jakoboeu.service.dataloader.GreenspaceSiteTypeLoader
import jakoboeu.service.dataloader.SurveySiteTypeLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class SiteType(
    val siteType: String,
    val source: SiteTypeSource,
)

enum class SiteTypeSource(val description: String) {
    BIOLOGICAL_SITE_DATASET("[bio mapped]"),
    GREENSPACE_GIS_DATASET("[os mappped]"),
    FIELD_SURVEY(" [field mapped]"),
    LLM_VISION_CLASSIFICATION("[llm mapped]")
}

@Configuration
class SiteTypeMappers(val greenspaceClassifier: GreenspaceClassifier) {

    @Bean
    fun biologicalSiteDataMapper(biologicalSiteTypeLoader: BiologicalSiteTypeLoader) =
        SiteTypeMapper(SiteTypeSource.BIOLOGICAL_SITE_DATASET, biologicalSiteTypeLoader, greenspaceClassifier)

    @Bean
    fun greenspaceTypeDataMapper(greenspaceSiteTypeLoader: GreenspaceSiteTypeLoader) =
        SiteTypeMapper(SiteTypeSource.GREENSPACE_GIS_DATASET, greenspaceSiteTypeLoader, greenspaceClassifier)

    @Bean
    fun surveyDataMapper(surveySiteTypeLoader: SurveySiteTypeLoader) =
        SiteTypeMapper(SiteTypeSource.FIELD_SURVEY, surveySiteTypeLoader, greenspaceClassifier)
}

class SiteTypeMapper(val siteTypeSource : SiteTypeSource, val siteTypeLoader: SiteTypeLoader, val greenspaceClassifier: GreenspaceClassifier) {
    val siteTypeLookup: Map<String,String> by lazy {
        greenspaceClassifier.reclassifyGreenspace(siteTypeLoader.distinctSiteTypes())
    }

    fun mappedSiteType(title: String): SiteType? {
        val siteType = siteTypeLoader.siteType(title)
        if (siteType != null && siteTypeLookup.containsKey(siteType)) {
            return SiteType(siteTypeLookup[siteType]!!, siteTypeSource)
        } else {
            return null
        }
    }
}
