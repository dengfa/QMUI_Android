package com.vincent.assetsplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin
import java.io.File

/**
 * Created by dengfa on 2021/11/24
 */
class AssetsPluginKtBs : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            println("vincent ${project.name} afterEvaluate")
            val target: File = project.file("src/main/assets")

            val plugins = project.plugins
            val extensions = project.extensions
            val variants: DomainObjectSet<out BaseVariant> = when {
                plugins.hasPlugin(LibraryPlugin::class) -> extensions.getByType(LibraryExtension::class).libraryVariants
                plugins.hasPlugin(AppPlugin::class) -> extensions.getByType(AppExtension::class).applicationVariants
                plugins.hasPlugin(TestPlugin::class) -> extensions.getByType(TestExtension::class).applicationVariants
                else -> throw GradleException("Unsupported project type")
            }

            variants.forEach {
                val outputDir = project.buildDir.resolve("generated").resolve("assets").resolve("src").resolve(it.name).resolve("kotlin")
                //1.判断是否已经生成过引文件，若已经生成过删除
                if (outputDir.exists()) {
                    outputDir.delete()
                }
                outputDir.mkdirs()
                //GenerateFileTask().run(target, outputDir, project.name)
                //it.addJavaSourceFoldersToModel(outputDir)
                val task = project.tasks.register("generateAssetsKotlinFile${it.name.capitalize()}", GenerateFileTask::class.java, target, outputDir, project.name)
                it.registerJavaGeneratingTask(task.get(), outputDir)
            }
        }

    }
}