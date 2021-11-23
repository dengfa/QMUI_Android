package com.assetsjournalist

import org.gradle.api.Plugin
import org.gradle.api.Project

class AssetsPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.task('hello') {
			doLast {
				println 'vincent hello AssetsPlugin'
			}
		}

		project.afterEvaluate {
			println 'vincent hello AssetsPlugin afterEvaluate'
		}
	}
}