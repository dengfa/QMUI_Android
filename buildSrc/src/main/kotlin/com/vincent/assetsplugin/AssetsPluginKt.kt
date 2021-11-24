package com.vincent.assetsplugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import java.io.File
import java.util.Locale

/**
 * Created by dengfa on 2021/11/24
 */
class AssetsPluginKt : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            println("vincent AssetsPluginKt afterEvaluate")

            //1.判断是否已经生成过引文件，若已经生成过删除
            val sourceOutputDir = project.file("${project.buildDir}/generated/assets")
            if (sourceOutputDir.exists()) {
                sourceOutputDir.delete()
            }

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

            // create type spec for object and include all properties
            val typeSpecBuilder = TypeSpec.objectBuilder("AssetFiles").addKdoc(
                "This class is generated using android-assets-journalist gradle plugin. \n" + "Do not modify this class because all changes will be overwritten"
            )
            tree.asFileTree.files.forEach() { file ->
                val property = PropertySpec.builder(getAssetAlias(file.name), String::class).addModifiers(KModifier.CONST)
                    .initializer("\"${getRelativePath(assetsDir, file)}\"").build()
                typeSpecBuilder.addProperty(property)
            }
            val objectSpec = typeSpecBuilder.build()

            // generating kt file
            FileSpec.builder("com.qmuiteam.qmuidemo", "AssetFiles").addType(objectSpec).build().writeTo(sourceOutputDir)
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