import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra["queryDslVersion"] = "5.0.0"

    dependencies {
//        classpath("gradle.plugin.com.ewerk.gradle.plugins:querydsl-plugin:1.0.10")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    }
}


plugins {
    val kotlinVersion = "1.7.21"

    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion

    id("com.adarshr.test-logger") version "3.0.0"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "java")
    apply(plugin = "kotlin-kapt")

    repositories {
        mavenCentral()
    }

    group = "com.oauth2"
    java.sourceCompatibility = JavaVersion.VERSION_17
}

subprojects {

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
        implementation("javax.inject:javax.inject:1")

        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.junit.jupiter:junit-jupiter-params")
        testImplementation(kotlin("test-junit5"))

        implementation("io.github.microutils:kotlin-logging:3.0.4")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }
}
