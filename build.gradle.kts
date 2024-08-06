import io.izzel.taboolib.gradle.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)
//    alias(libs.plugins.detekt.compiler)

    alias(libs.plugins.taboolib)
}

repositories {
    if (System.getenv("CI")?.toBoolean() != true) {
        maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    }
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
        content {
            includeGroup("com.velocitypowered")
        }
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        content {
            includeGroup("dev.vankka")
        }
    }
    maven("https://repo.opencollab.dev/main/")
    maven {
        url = uri("https://repo.william278.net/velocity/")
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
        install(CONFIGURATION, LANG, CHAT)
        install(VELOCITY)
    }
    version { taboolib = "6.1.2-beta11" }
    relocate("kotlinx.serialization", "avm.kotlinx.serialization")
    relocate("dev.vankka.enhancedlegacytext", "avm.dev.vankka.enhancedlegacytext")
    relocate("com.charleskorn.kaml", "avm.com.charleskorn.kaml")
}

dependencies {
    detektPlugins(libs.detekt)

    compileOnly(libs.velocity.api)
    compileOnly(libs.velocity.proxy)
    compileOnly(libs.floodgate)
    compileOnly(libs.netty)

    compileOnly(kotlin("stdlib"))

    taboo(libs.kaml)

    taboo(libs.kotlin.serialization.json) { isTransitive = false }
    taboo(libs.enhanced.legacy.text) { isTransitive = false }

    taboo(libs.asm)
    taboo(libs.asm.util)
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
