package com.crossbowffs.quotelock

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.util.Locale

private val coverageExclusions = listOf(
    // Android
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*_Hilt*.class",
    "**/Hilt_*.class",
    "**/*\$InjectAdapter.class",
    "**/*\$ModuleAdapter.class",
    "**/*\$ViewInjector*.class",
    "**/*_GeneratedInjector*.class",
    "*/*_GeneratedInjector*.*",
    "**/*_MembersInjector.class",
    "*/*_MembersInjector*.*"
)

private fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
}

private fun Project.configureJacocoTask(
    taskName: String,
    classDirs: Iterable<*>,
    sourceDirs: Iterable<*>,
    execDirs: Iterable<*>,
    outputDir: Provider<Directory>? = null,
) {
    tasks.register(taskName, JacocoReport::class) {
        group = "coverage"

        classDirectories.setFrom(classDirs)
        reports {
            xml.apply {
                required.set(true)
                outputDir?.let { outputLocation.set(it.get().dir("xml").file("coverage_report.xml")) }
            }
            html.apply {
                required.set(true)
                outputDir?.let { outputLocation.set(it.get().dir("html")) }
            }
            csv.apply {
                required.set(true)
                outputDir?.let { outputLocation.set(it.get().dir("csv").file("coverage_report.csv")) }
            }
        }

        sourceDirectories.setFrom(sourceDirs)

        executionData.setFrom(execDirs)
    }
}

internal fun Project.configureManualJacoco(
    androidComponentsExtension: AndroidComponentsExtension<*, *, *>,
) {
    configure<JacocoPluginExtension> {
        toolVersion = libs.versions.jacoco.get().toString()
    }

    fun Project.filterValidModuleChildren(): List<Project> =
        rootProject.allprojects.filter { subproject ->
            val buildFile = subproject.file("build.gradle")
            val buildFileKts = subproject.file("build.gradle.kts")
            buildFile.exists() || buildFileKts.exists()
        }

    androidComponentsExtension.onVariants { variant ->
        if (variant.buildType == "release") {
            return@onVariants
        }
        val classSubDirs = listOf(
            "tmp/kotlin-classes/${variant.name}",
            "intermediates/javac/${variant.name}/classes"
        )
        val sourceSubDirs = listOf(
            "src/main/java",
            "src/main/kotlin"
        ) + variant.productFlavors.map(Pair<String, String>::second).distinct().flatMap {
            listOf(
                "src/${it}/java",
                "src/${it}/kotlin"
            )
        }

        if (androidComponentsExtension is ApplicationAndroidComponentsExtension) {
            val projectClassDirs = filterValidModuleChildren().flatMap { subproject ->
                val objFactory = subproject.objects
                val subBuildDir = subproject.layout.buildDirectory.get().asFile
                classSubDirs.map { classSubDir ->
                    objFactory.fileTree().setDir("${subBuildDir}/${classSubDir}")
                        .exclude(coverageExclusions)
                }
            }
            val projectSourceDirs = filterValidModuleChildren().flatMap { subproject ->
                val subSourceDir = subproject.projectDir
                if (subproject.projectDir == rootProject.projectDir) {
                    return@flatMap emptyList()
                }
                sourceSubDirs.map { sourceSubDir ->
                    "$subSourceDir/${sourceSubDir}"
                }
            }

            configureJacocoTask(
                "createProject${variant.name.capitalize()}ManualCoverageReport",
                projectClassDirs,
                projectSourceDirs,
                rootProject.fileTree("$rootDir/build/code_coverage")
                    .matching {
                        include("**/*.exec")
                        include("**/*.ec")
                    },
                rootProject.layout.buildDirectory.dir("reports/jacoco/${variant.name}")
            )
        }
        configureJacocoTask(
            "create${variant.name.capitalize()}ManualCoverageReport",
            classSubDirs.map { classSubDir ->
                objects.fileTree()
                    .setDir("${layout.buildDirectory.get().asFile}/${classSubDir}")
                    .exclude(coverageExclusions)
            },
            sourceSubDirs.map { sourceSubDir ->
                objects.fileTree().setDir("$projectDir/${sourceSubDir}")
            },
            fileTree("$rootDir/build/code_coverage")
                .matching {
                    include("**/*.exec")
                    include("**/*.ec")
                },
            layout.buildDirectory.dir("reports/jacoco/${variant.name}")
        )
    }
}
