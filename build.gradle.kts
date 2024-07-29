import io.izzel.taboolib.gradle.*

plugins {
    val kotlinVersion = "2.0.0"
    kotlin("jvm").version(kotlinVersion)
    kotlin("plugin.serialization").version(kotlinVersion)

    val detektVersion = "1.23.6"
    id("io.gitlab.arturbosch.detekt").version(detektVersion)
//    id("io.github.detekt.gradle.compiler-plugin").version(detektVersion)

    val taboolibVersion = "2.0.13"
    id("io.izzel.taboolib").version(taboolibVersion)
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
    val detektVersion = "1.23.6"
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${detektVersion}")

    val velocityVersion = "3.3.0-SNAPSHOT"
    compileOnly("com.velocitypowered:velocity-api:$velocityVersion")

    val floodgateVersion = "2.2.3-SNAPSHOT"
    compileOnly("org.geysermc.floodgate:api:$floodgateVersion")

    val nettyVersion = "4.1.112.Final"
    compileOnly("io.netty:netty-all:$nettyVersion")

    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))

    val kamlVersion = "0.61.0"
    taboo("com.charleskorn.kaml:kaml:$kamlVersion")

    val kotlinxSerializationVersion = "1.7.1"
    taboo("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion") {
        isTransitive = false
    }

    val enhancedLegacyTextVersion = "2.0.0-SNAPSHOT"
    taboo("dev.vankka:enhancedlegacytext:$enhancedLegacyTextVersion") {
        isTransitive = false
    }

    val asmVersion = "9.7"
    taboo("org.ow2.asm:asm:$asmVersion")
    taboo("org.ow2.asm:asm-util:$asmVersion")

    val bytebuddyagentVersion = "1.14.18"
    taboo("net.bytebuddy:byte-buddy-agent:$bytebuddyagentVersion")
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
