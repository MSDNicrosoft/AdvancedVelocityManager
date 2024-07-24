import io.izzel.taboolib.gradle.*

plugins {
    val kotlinVersion = "2.0.0"
    kotlin("jvm").version(kotlinVersion)
    kotlin("plugin.serialization").version(kotlinVersion)

    val detektVersion = "1.23.6"
    id("io.gitlab.arturbosch.detekt").version(detektVersion)
//    id("io.github.detekt.gradle.compiler-plugin").version(detektVersion)

    val taboolibVersion = "2.0.12"
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
    maven("https://repo.opencollab.dev/main/" )
    maven("https://jitpack.io")
    mavenCentral()
}

taboolib {
    description {
        name("AdvancedVelocityManager")
        contributors {
            name("MSDNicrosoft")
        }
    }
    env {
        install(UNIVERSAL, VELOCITY)
    }
    version { taboolib = "6.1.2-beta11" }
//    relocate("okhttp3", "avm.okhttp3")
}

dependencies {
    val velocity_version: String by project
    compileOnly("com.velocitypowered:velocity-api:${velocity_version}")
    val detektVersion = "1.23.6"
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${detektVersion}")

//    val floodgate_version: String by project
//    compileOnly("org.geysermc.floodgate:api:${floodgate_version}")
//
//    val netty_version: String by project
//    compileOnly("io.netty:netty-all:${netty_version}")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))

    val hoplite_version: String by project
    implementation("com.sksamuel.hoplite:hoplite-core:${hoplite_version}")
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
