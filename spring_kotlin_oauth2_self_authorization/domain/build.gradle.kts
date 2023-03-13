val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks
bootJar.enabled = false
val jar: Jar by tasks
jar.enabled = true

plugins {
    val kotlinVersion = "1.7.21"
    val springbootVersion = "3.0.0"
    id("org.springframework.boot") version springbootVersion
    id("io.spring.dependency-management") version "1.1.0"
    id("com.ewerk.gradle.plugins.querydsl") version "1.0.10"

    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

apply(plugin = "com.ewerk.gradle.plugins.querydsl")

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Entity")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("com.vladmihalcea:hibernate-types-60:2.20.0")
    implementation("com.infobip:infobip-spring-data-jpa-querydsl-boot-starter:8.0.0")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta") {
        exclude(group = "com.google.guava")
    }

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// querydsl 추가 시작
var querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets["main"].withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
    kotlin.srcDir("$buildDir/generated/source/kapt/main")
}

configurations {
    compileOnly.apply {
        extendsFrom(configurations.annotationProcessor.get())
    }
    create("testCompile").apply {
        extendsFrom(configurations.compileOnly.get())
    }
}
