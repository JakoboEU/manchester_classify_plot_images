package jakoboeu.service.dataloader

interface SiteTypeLoader {

    fun distinctSiteTypes() : Set<String>
    fun siteType(title: String) : String?

}