import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinBenchmarkVersion = "0.4.2"

plugins {
    id("org.springframework.boot") version "2.6.4"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.10"
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

group = "com.slopez"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

benchmark {
    targets {
        register("test")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation:2.6.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$kotlinBenchmarkVersion")
    }
}
