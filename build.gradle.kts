plugins {
	java
	id("org.springframework.boot") version "3.5.11"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "4.4.1.3373"
	id("com.google.protobuf") version "0.9.4"
	idea
}

idea {
	module {
		sourceDirs.plusAssign(file("build/generated/source/proto/main/java"))
		sourceDirs.plusAssign(file("build/generated/source/proto/main/grpc"))
		generatedSourceDirs.plusAssign(file("build/generated/source/proto/main/java"))
		generatedSourceDirs.plusAssign(file("build/generated/source/proto/main/grpc"))
	}
}

group = "id.ac.ui.cs.advprog"
version = "0.0.1-SNAPSHOT"
description = "bidmart-catalogue-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	testRuntimeOnly("com.h2database:h2")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	implementation("io.grpc:grpc-protobuf:1.62.2")
	implementation("io.grpc:grpc-stub:1.62.2")
	implementation("com.google.protobuf:protobuf-java:3.25.1")
}

sourceSets {
	main {
		java {
			srcDirs(
				"${layout.buildDirectory.get()}/generated/source/proto/main/java",
				"${layout.buildDirectory.get()}/generated/source/proto/main/grpc",
			)
		}
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:3.25.1"
	}
	plugins {
		create("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.62.2"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				create("grpc") {}
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sonar {
	properties {
		property("sonar.projectKey", "advprog-2026-A17-project_bidmart-catalogue-service")
		property("sonar.organization", "advprog-2026-a17-project")
		property("sonar.host.url", "https://sonarcloud.io")
	}
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude("**/grpc/**")
				exclude("**/BidmartCatalogueServiceApplication*")
			}
		}),
	)
}
