import io.izzel.taboolib.gradle.*
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)
//    alias(libs.plugins.detekt.compiler)

    alias(libs.plugins.taboolib)

    alias(libs.plugins.grgit)
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
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "PaperMC"
        content {
            includeGroup("com.velocitypowered")
        }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        content {
            includeGroup("dev.vankka")
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
    mavenCentral()
}

taboolib {
    description {
        name("AdvancedVelocityManager")
        desc("AdvancedVelocityManager is a modern and advanced Velocity plugin")
        links {
            name("homepage").url("https://github.com/MSDNicrosoft/AdvancedVelocityManager")
        }
        contributors {
            name("MSDNicrosoft")
        }
    }
    env {
        install(CommandHelper, I18n)
        install(Velocity)
    }
    version {
        taboolib = "6.2.3-b217935"
        coroutines = null
    }
    relocate("kotlinx.serialization", "avm.kotlinx.serialization")
    relocate("dev.vankka.enhancedlegacytext", "avm.dev.vankka.enhancedlegacytext")
    relocate("com.charleskorn.kaml", "avm.com.charleskorn.kaml")
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    compileOnly(libs.bundles.velocity)
    compileOnly(libs.floodgate)
    compileOnly(libs.netty)

    compileOnly(kotlin("stdlib"))

    taboo(libs.kaml)

    taboo(libs.kotlin.serialization.json) { isTransitive = false }
    taboo(libs.enhanced.legacy.text) { isTransitive = false }

    taboo(libs.bundles.asm)
    taboo(libs.byte.buddy.agent)
}

detekt {
    parallel = true
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
}

tasks {
    compileKotlin {
        dependsOn("detekt")
    }
}
