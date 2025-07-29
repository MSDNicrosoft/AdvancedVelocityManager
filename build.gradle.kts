import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    maven("https://repo.william278.net/velocity/") {
        content {
            includeGroup("com.velocitypowered")
        }
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    annotationProcessor(libs.velocity.api)

    compileOnly(libs.bundles.velocity)
    compileOnly(libs.bundles.asm)
    compileOnly(libs.floodgate)
    compileOnly(libs.netty)
    compileOnly(libs.fastutil)
    compileOnly(kotlin("stdlib"))

    implementation(libs.kaml)
    implementation(libs.kotlin.serialization.json) { isTransitive = false }
    implementation(libs.byte.buddy.agent)
    implementation(libs.bundles.kavaref)
}

yamlang {
    targetSourceSets = listOf(sourceSets.main.get())
    inputDir.set("lang")
}

detekt {
    parallel = true
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    compileJava {
        targetCompatibility = "17"
    }
    jar {
        archiveFileName = "${rootProject.name}-${rootProject.version}-unshaded.jar"
    }
    shadowJar {
        minimize()
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
    compileKotlin {
        dependsOn(detekt)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }
}
