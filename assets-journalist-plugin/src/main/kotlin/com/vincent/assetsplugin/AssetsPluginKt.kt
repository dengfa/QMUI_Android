package com.vincent.assetsplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestExtension
import com.android.build.gradle.TestPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.hasPlugin
import java.io.File
import java.util.Locale

/**
 * Created by dengfa on 2021/11/24
 */
class AssetsPluginKt : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            println("vincent AssetsPluginKt afterEvaluate")


            val assetsDir = project.file("src/main/assets")

            //2.遍历assets文件
            val tree: FileTree = project.fileTree("src/main/assets")

            // 访问树结构的每个结点
            /*tree.visit {element ->
                //println "$element.relativePath => $element.file"
                println "$element.relativePath => ${getRelativePath(assetsDir, element.file)}"
            }*/


            tree.asFileTree.files.forEach {
                println("${getAssetAlias(it.name)} => ${getRelativePath(assetsDir, it)}")
            } //3.过滤不需要生成索引的资源（例如：Lottie的资源文件）

            //4.利用kotlinpoet生成资源索引文件
            val plugins = project.plugins
            val extensions = project.extensions

            val variants: DomainObjectSet<out BaseVariant> = when {
                plugins.hasPlugin(LibraryPlugin::class) -> extensions.getByType(LibraryExtension::class).libraryVariants
                plugins.hasPlugin(AppPlugin::class) -> extensions.getByType(AppExtension::class).applicationVariants
                plugins.hasPlugin(TestPlugin::class) -> extensions.getByType(TestExtension::class).applicationVariants
                else -> throw GradleException("Unsupported project type")
            }

            variants.forEach {
                println("variant name : ${it.name}")


                val sourceOutputDir =
                    project.buildDir.resolve("generated").resolve("assets").resolve("src").resolve("kotlin") //1.判断是否已经生成过引文件，若已经生成过删除
                if (sourceOutputDir.exists()) {
                    sourceOutputDir.delete()
                }

                // create type spec for object and include all properties
                val typeSpecBuilder = TypeSpec.objectBuilder("AssetFiles")
                    .addKdoc("This class is generated using android-assets-journalist gradle plugin. \n" + "Do not modify this class because all changes will be overwritten")
                tree.asFileTree.files.forEach() { file ->
                    val property = PropertySpec.builder(getAssetAlias(file.name), String::class).addModifiers(KModifier.CONST)
                        .initializer("\"${getRelativePath(assetsDir, file)}\"").build()
                    typeSpecBuilder.addProperty(property)
                }
                val objectSpec = typeSpecBuilder.build()

                // generating kt file
                FileSpec.builder("com.qmuiteam.qmuidemo", "AssetFiles").addType(objectSpec).build().writeTo(sourceOutputDir)

                it.addJavaSourceFoldersToModel(sourceOutputDir)
            }
        }

    }

    // https://gist.github.com/ysb33r/5804364
    private fun getRelativePath(assetDir: File, assetFile: File): String {
        return assetDir.toURI().relativize(assetFile.toURI()).toString()
    }

    private fun getAssetAlias(name: String): String {
        val upName = name.trim().toUpperCase(Locale.ROOT)
        val suffixStart = upName.lastIndexOf(".")
        return if (suffixStart == -1) upName else upName.substring(0, suffixStart)
    }
}