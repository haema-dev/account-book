import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// gradle 에서 yml 을 읽어오기 위한 설정
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
// CompileTime 이 아닌 Runtime 시에 동작하므로 buildscript 에 의존성 추가
// Gradle 초기 설정 단계에서 클래스 경로가 설정되어야 하므로 buildscript 블록은 스크립트 파일의 상단에 위치
buildscript {
	dependencies {
		classpath("com.fasterxml.jackson.core:jackson-databind:2.15.0")
		classpath("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")
	}
}

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"

	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"
}

group = "book"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

// 메인 프로젝트와 경로를 맞춰줘야 런타임 시에 정상적으로 주입이 된다
sourceSets {
	main {
		resources {
			srcDir("src/main/resources")
			srcDir("security-module/account-bank/src/main/resources")
		}
	}
}



repositories {
	mavenCentral()
}

dependencies {
	// dependency-management 가 관리하므로 버전 명시할 필요 없음
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	// Database PostgresSQL
	runtimeOnly("org.postgresql:postgresql")

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") // openApi 와 SwaggerUI 를 통합하기 위해 추가
	runtimeOnly("org.springdoc:springdoc-openapi-kotlin:1.7.0") // kotlin 으로 해석하기 위해 추가

	// Jackson Yaml
	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

// gradle 에서 yml 을 읽어오기 위한 설정
tasks.register("readYaml") {
	doLast {
		val file = File("security-module/account-bank/src/main/resources/application.yml")
		val mapper = ObjectMapper(YAMLFactory())
		val config: Map<*, *> = mapper.readValue(file, Map::class.java)
		println(config)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}