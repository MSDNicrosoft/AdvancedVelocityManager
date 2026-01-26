import com.google.gson.Gson
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI
import java.net.URL

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)
//    alias(libs.plugins.detekt.compiler)

    alias(libs.plugins.grgit)
    alias(libs.plugins.yamlang)
    alias(libs.plugins.shadow)
}

base {
    val grgit: Grgit? by lazy(LazyThreadSafetyMode.NONE) {
        runCatching {
            grgitService.service.get().grgit
        }.getOrNull()
    }

    fun getLatestCommit(): Commit? = grgit?.head()

    fun getLatestTagCommit(): Commit = grgit!!.tag.list().last().commit

    fun getUnreleasedCommits(): Int = grgit!!.log {
        range(getLatestTagCommit(), getLatestCommit())
    }.size

    fun getVersion(): String = buildString {
        if (grgit == null || getLatestCommit() == null) {
            append("-nogit")
            return@buildString
        }

        if (!grgit!!.status().isClean) {
            append("-DEV")
            return@buildString
        }

        val unreleasedCommits = getUnreleasedCommits()
        if (grgit!!.branch.current().name != "master" || unreleasedCommits > 0) {
            append("-SNAPSHOT")

            if (unreleasedCommits > 0) {
                append("+$unreleasedCommits")
            }
        }

        append("-${getLatestCommit()!!.abbreviatedId}")
    }
    version = "$version${getVersion()}"
}

repositories {
    mavenCentral()
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
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
    compileOnly(kotlin("stdlib"))

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
        archiveFileName = "${rootProject.name}-${rootProject.version}-unshaded.jar"
    }
    shadowJar {
        minimizeJar = true
        archiveClassifier = null

        doFirst {
            exclude("META-INF/maven/**")
            exclude("META-INF/versions/**")
            exclude("META-INF/proguard/**")
            exclude("META-INF/com.android.tools/**")
        }

        relocate("kotlin", "avm.kotlin")
        relocate("kotlinx.serialization", "avm.kotlinx.serialization")
        relocate("com.charleskorn.kaml", "avm.com.charleskorn.kaml")
        relocate("com.highcapable.kavaref", "avm.com.highcapable.kavaref")
    }
    processResources {
        filesMatching("velocity-plugin.json") {
            expand("version" to version)
        }
    }
    compileJava {
        targetCompatibility = "21"
    }
    compileKotlin {
        dependsOn(detekt)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
    clean {
        doLast {
            runDir.deleteRecursively()
        }
    }

    @Suppress("UNCHECKED_CAST")
    val downloadVelocity by registering {
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

    val runVelocity by registering(JavaExec::class) {
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

        jvmArgs(
            "-XX:+UseG1GC",
            "-XX:G1HeapRegionSize=4M",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+ParallelRefProcEnabled",
            "-XX:+AlwaysPreTouch",
            "-XX:MaxInlineLevel=15"
        )

        classpath = files(velocityJar)
    }
}
