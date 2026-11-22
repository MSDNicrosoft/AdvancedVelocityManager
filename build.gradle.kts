import com.google.gson.Gson
import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.net.URL

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)

    alias(libs.plugins.git.version)
    alias(libs.plugins.yamlang)
    alias(libs.plugins.shadow)
}

val releaseVersion = version.toString()

@Suppress("UNCHECKED_CAST")
val versionDetails = runCatching { (extra["versionDetails"] as Closure<VersionDetails>).invoke() }.getOrNull()

version = buildString {
    append(releaseVersion)
    if (versionDetails == null) {
        append("-nogit")
        return@buildString
    }
    if (versionDetails.version.endsWith(".dirty")) {
        append("-DEV")
        return@buildString
    }
    val distance = versionDetails.commitDistance
    if (versionDetails.branchName != "master" || distance > 0) {
        append("-SNAPSHOT")
        if (distance > 0) {
            append("+$distance")
        }
    }
    append("-${versionDetails.gitHash}")
}

repositories {
    mavenCentral()
    if (!providers.environmentVariable("CI").getOrElse("false").toBoolean()) {
        maven("https://maven.aliyun.com/repository/public")
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
        content {
            includeGroup("com.velocitypowered")
        }
    }
    maven("https://repo.opencollab.dev/main/") {
        content {
            includeGroupByRegex("org.geysermc.*")
        }
    }
    maven("https://maven.msdnicrosoft.work/velocity/") {
        content {
            includeGroup("com.velocitypowered")
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    compileOnly(libs.bundles.velocity)
    compileOnly(libs.bundles.asm)
    compileOnly(libs.floodgate)
    compileOnly(libs.netty)
    compileOnly(libs.fastutil)

    implementation(libs.kaml)
    implementation(libs.kotlin.serialization.json) { isTransitive = false }
    implementation(libs.byte.buddy.agent)
    implementation(libs.bundles.kavaref) { isTransitive = false }
}

detekt {
    config.setFrom(project.rootDir.resolve("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    autoCorrect = true
}

yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir = "lang"
}

tasks {
    val runDir: File = project.rootDir.resolve("run")

    build {
        dependsOn(shadowJar)
    }
    jar {
        archiveFileName.set("${rootProject.name}-${rootProject.version}-unshaded.jar")
    }
    shadowJar {
        minimizeJar = true
        archiveClassifier = null
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()

        exclude(
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/proguard/**",
            "META-INF/com.android.tools/**",
        )

        relocate("kotlin", "avm.kotlin")
        relocate("kotlinx.serialization", "avm.kotlinx.serialization")
        relocate("com.charleskorn.kaml", "avm.com.charleskorn.kaml")
        relocate("com.highcapable.kavaref", "avm.com.highcapable.kavaref")
    }
    processResources {
        val tokens: Map<String, String> = mapOf("version" to version.toString())
        inputs.properties(tokens)
        filteringCharset = "UTF-8"
        filesMatching("velocity-plugin.json") {
            expand(tokens)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
    compileKotlin {
        dependsOn(detekt)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }
    clean {
        doLast {
            runDir.deleteRecursively()
        }
    }

    @Suppress("UNCHECKED_CAST")
    val downloadVelocity = register("downloadVelocity") {
        description = "Downloads the latest Velocity server JAR"
        group = "velocity"

        val version: String = libs.versions.velocity.get()
        val velocityJar: File = file("$runDir/velocity-server-$version.jar")
        val gson = Gson()

        outputs.file(velocityJar)

        doLast {
            if (!runDir.exists()) runDir.mkdirs()
            if (velocityJar.exists() && velocityJar.length() > 0) return@doLast

            val buildsUrl: URL = URI("https://fill.papermc.io/v3/projects/velocity/versions/$version/builds").toURL()
            val latestBuild: Map<String, Any> = gson
                .fromJson<List<Map<String, Any>>>(buildsUrl.readText(), List::class.java)
                .first()

            val downloads: Map<String, Map<String, Any>> = latestBuild["downloads"] as Map<String, Map<String, Any>>
            val serverDefault: Map<String, Any> = downloads["server:default"]!!
            val downloadUrl: URL = URI(serverDefault["url"] as String).toURL()


            println("Downloading Velocity server $version...")

            try {
                downloadUrl.openStream().use { input ->
                    velocityJar.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                println("Failed to download Velocity server: ${e.message}")
                throw e
            }
        }
    }

    val runVelocity = register<JavaExec>("runVelocity") {
        dependsOn(shadowJar, downloadVelocity)
        description = "Runs the velocity server."
        group = "velocity"

        val version: String = libs.versions.velocity.get()
        val pluginsDir: File = file("run/plugins")
        val velocityJar: File = file("$runDir/velocity-server-$version.jar")

        standardInput = System.`in`
        standardOutput = System.out
        errorOutput = System.err

        workingDir = runDir

        doFirst {
            if (!pluginsDir.exists()) pluginsDir.mkdirs()

            val pluginJar: File = shadowJar.get().archiveFile.get().asFile
            if (pluginJar.exists()) {
                copy {
                    from(pluginJar)
                    into(pluginsDir)
                }
            }

            if (!velocityJar.exists()) throw GradleException("Velocity server does not exist: ${velocityJar.absolutePath}")
        }

        mainClass = "com.velocitypowered.proxy.Velocity"

        systemProperty("net.kyori.adventure.text.warnWhenLegacyFormattingDetected", true)
        systemProperty("file.encoding", "UTF-8")
        systemProperty("stdout.encoding", "UTF-8")

        classpath = files(velocityJar)
    }
}
