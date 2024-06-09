import io.izzel.taboolib.gradle.*

plugins {
    val kotlinVersion = "2.0.0"
    kotlin("jvm").version(kotlinVersion)
    kotlin("plugin.serialization").version(kotlinVersion)

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
    maven("https://maven.tryformation.com/releases") {
        // optional but it speeds up the gradle dependency resolution
        content {
            includeGroup("com.jillesvangurp")
        }
    }
    mavenCentral()
}

taboolib {
    description {
        name = "AdvancedVelocityWhitelist"
        contributors {
            name = "MSDNicrosoft"
        }
    }
    env {
        install(UNIVERSAL, VELOCITY)
    }
    version { taboolib = "6.1.1-beta26" }
}

dependencies {
    val velocity_version: String by project
    compileOnly("com.velocitypowered:velocity-api:${velocity_version}")

//    val floodgate_version: String by project
//    compileOnly("org.geysermc.floodgate:api:${floodgate_version}")
//
//    val netty_version: String by project
//    compileOnly("io.netty:netty-all:${netty_version}")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))

    val json_dsl_version: String by project
    implementation("com.jillesvangurp:json-dsl:${json_dsl_version}")
}
