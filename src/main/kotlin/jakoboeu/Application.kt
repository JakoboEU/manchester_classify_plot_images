package jakoboeu

import jakoboeu.ai.GreenspaceClassifier
import jakoboeu.ai.ImageClassifier
import jakoboeu.model.PlotImage
import jakoboeu.service.FileService
import jakoboeu.service.GreenspaceTypeLoader
import jakoboeu.service.ImageResizer
import jakoboeu.service.SurveyDataLoader
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

@SpringBootApplication
class Application {
    @Bean
    fun runner(worker: Worker) = ApplicationRunner {
        worker.classifyImages()
    }
}

fun main(args: Array<String>) {
    if (System.getenv("GS_MANC_CLASS_OPENAI_API_KEY") == null) {
        throw IllegalStateException("Missing KEY in env vars");
    }
    val app = SpringApplication(Application::class.java)
    app.setWebApplicationType(WebApplicationType.NONE)
    val ctx: ConfigurableApplicationContext? = app.run(*args)
    val code = SpringApplication.exit(ctx);
    exitProcess(code);
}

@Service
class Worker(
    val fileService: FileService,
    val imageResizer: ImageResizer,
//    val greenspaceTypeLoader: GreenspaceTypeLoader,
//    val greenspaceClassifier: GreenspaceClassifier,
//    val surveyDataLoader: SurveyDataLoader,
    val imageClassifier: ImageClassifier,
) {
    fun classifyImages() {
        val plotImageFiles = fileService.listAllPlotImageFiles()
            .map {
                PlotImage.create(
                    it.title,
                this.imageResizer.resizeImage(it.file)
                )
            }.map {
                Pair(
                    it.title,
                    imageClassifier.classifyImage(it.file))
            }.forEach {
                println(it.component1())
                println(it.component2())
            }
    }
}