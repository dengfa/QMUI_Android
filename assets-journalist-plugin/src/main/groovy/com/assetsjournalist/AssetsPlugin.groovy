package com.assetsjournalist

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree

/**
 * todo 扩展
 * 1.处理Lottie文件;
 * 2.支持配置过滤规则*/
class AssetsPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.afterEvaluate {
			println 'vincent hello com.vincent.AssetsPluginTest afterEvaluate'

			//1.判断是否已经生成过引文件，若已经生成过删除
			def sourceOutputDir = project.file("$project.buildDir/generated/assets")
			if (sourceOutputDir.exists()) {
				println('Deleting Assets.java ...')
				sourceOutputDir.deleteDir()
			}

			File assetsDir = project.file('src/main/assets')

			//2.遍历assets文件
			FileTree tree = project.fileTree(dir: 'src/main/assets')

			// 访问树结构的每个结点
			/*tree.visit {element ->
                //println "$element.relativePath => $element.file"
                println "$element.relativePath => ${getRelativePath(assetsDir, element.file)}"
            }*/


			tree.asFileTree.files.forEach {
				println "${getAssetAlias(it.name)} => ${getRelativePath(assetsDir, it)}"
			}
			//3.过滤不需要生成索引的资源（例如：Lottie的资源文件）

			//4.利用kotlinpoet生成资源索引文件

			// create type spec for object and include all properties
			def typeSpecBuilder = TypeSpec.objectBuilder("AssetFiles")
				.addKdoc("This class is generated using android-assets-journalist gradle plugin. \n" +
					"Do not modify this class because all changes will be overwritten")
			tree.asFileTree.files.forEach() { file ->
				def property = PropertySpec.builder(getAssetAlias(file.name), String.class)
					.addModifiers(KModifier.CONST)
					.initializer("\"${getRelativePath(assetsDir, file)}\"")
					.build()
				typeSpecBuilder.addProperty(property)
			}
			def objectSpec = typeSpecBuilder.build()

			// generating kt file
			FileSpec.builder("com.qmuiteam.qmuidemo", "AssetFiles")
				.addType(objectSpec)
				.build()
				.writeTo(sourceOutputDir)
		}
	}

	String getRelativePath(File assetDir, File assetFile) {
		// https://gist.github.com/ysb33r/5804364
		assetDir.toURI().relativize(assetFile.toURI()).toString()
	}

	String getAssetAlias(String name) {
		name = (name.trim() ?: 'EMPTY').toUpperCase(Locale.ROOT)
		int suffixStart = name.lastIndexOf '.'
		suffixStart == -1 ? name : name.substring(0, suffixStart)
	}
}