package com.vincent.assetsplugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Builder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * Created by dengfa on 2021/11/30
 */
open class GenerateFileTask @Inject constructor(@get:InputDirectory val assetsDir: File, @get:InputDirectory val outputDir: File, @get:Input val moduleName: String) :
    DefaultTask() {

    @TaskAction
    fun run() {
        println("GenerateFileTask.run moduleName $moduleName")
        val fileBuilder = FileSpec.builder("com.github.vincent", "AssetFiles")
        val typeSpecBuilder = TypeSpec.objectBuilder("AssetFiles")
            .addKdoc("This class is generated using ${AssetsPluginKtBs::class.simpleName} gradle plugin. \n" + "Do not modify this class because all changes will be overwritten")
        val typeSpec = bfsFolder(assetsDir, typeSpecBuilder)
        fileBuilder.addType(typeSpec).build().writeTo(outputDir)
    }

    private fun getRelativePath(fileReto: File, file: File): String {
        return fileReto.toURI().relativize(file.toURI()).toString()
    }

    private fun getAssetAlias(name: String): String {
        val upName = name.trim().toUpperCase(Locale.ROOT)
        val suffixStart = upName.lastIndexOf(".")
        return if (suffixStart == -1) upName else upName.substring(0, suffixStart)
    }

    private fun bfsFolder(target: File, typeSpecBuilder: Builder): TypeSpec {
        val folders = arrayListOf<File>()
        target.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                folders.add(file)
            } else {
                val property = PropertySpec.builder(getAssetAlias(file.name), String::class).addModifiers(KModifier.CONST)
                    .initializer("\"${getRelativePath(assetsDir, file)}\"").build()
                typeSpecBuilder.addProperty(property)
            }
        }
        folders.forEach { file ->
            val objBuilder = TypeSpec.objectBuilder(file.name)
            val typeSpec = bfsFolder(file, objBuilder)
            typeSpecBuilder.addType(typeSpec)
        }
        return typeSpecBuilder.build()
    }
}