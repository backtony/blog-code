
val jar: Jar by tasks
jar.enabled = false

plugins {
    val kotlinVersion = "1.7.21"
    val springbootVersion = "3.0.0"
    id("org.springframework.boot") version springbootVersion
    id("io.spring.dependency-management") version "1.1.0"

    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

apply(plugin = "org.springframework.boot")
apply(plugin = "io.spring.dependency-management")
apply(plugin = "org.jetbrains.kotlin.plugin.spring")


dependencies {
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("mysql:mysql-connector-java")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

    testImplementation("org.springframework.security:spring-security-test:6.0.1")

    implementation("com.vladmihalcea:hibernate-types-60:2.20.0")
    implementation("com.infobip:infobip-spring-data-jpa-querydsl-boot-starter:8.0.0")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta") {
        exclude(group = "com.google.guava")
    }

    kapt("org.springframework.boot:spring-boot-configuration-processor")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")

    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

