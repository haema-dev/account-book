import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// gradle 에서 yml 을 읽어오기 위한 설정
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
// application.yml 파일을 주입시키기 위한 설정
import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import org.jooq.meta.jaxb.Logging

// CompileTime 이 아닌 Runtime 시에 동작하므로 buildscript 에 의존성 추가
// Gradle 초기 설정 단계에서 클래스 경로가 설정되어야 하므로 buildscript 블록은 스크립트 파일의 상단에 위치
buildscript {
	dependencies {
		classpath("com.fasterxml.jackson.core:jackson-databind:2.15.0")
		classpath("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")
	}
}

// Version & yml path information
object BuildConfig {
	object Project {
		const val GROUP = "book"
		const val VERSION = "0.0.1-SNAPSHOT"
		const val JVM_TARGET = "17"
	}
	object Versions {
		const val KOTLIN = "1.8.22"
		const val SPRING_BOOT = "3.1.2"
		const val SPRING_DEPENDENCY = "1.1.2"
		const val JOOQ = "8.2"
		const val OPENAPI_KOTLIN = "1.7.0"
		const val OPENAPI_STARTER = "2.1.0"
		const val POSTGRESQL = "42.5.1"
		const val JACKSON = "2.15.0"
	}
	object Paths { const val CONFIG_PATH = "security-module/account-bank/src/main/resources/application.yml" }
}

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"

	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"

	id("nu.studer.jooq") version "8.2"
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

//dependencies {
//	// dependency-management 가 관리하므로 버전 명시할 필요 없음
//	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//	implementation("org.springframework.boot:spring-boot-starter-web")
//	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("org.jetbrains.kotlin:kotlin-reflect")
//	// Database PostgresSQL
//	runtimeOnly("org.postgresql:postgresql")
//
//	// Swagger
//	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0") // openApi 와 SwaggerUI 를 통합하기 위해 추가
//	runtimeOnly("org.springdoc:springdoc-openapi-kotlin:1.7.0") // kotlin 으로 해석하기 위해 추가
//
//	// Jackson Yaml
//	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
//	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0")
//
//	// Jooq
//	jooqGenerator ("org.postgresql:postgresql:42.5.1")
//
//	// Test
//	testImplementation("org.springframework.boot:spring-boot-starter-test")
//}

object DependencyManagement {
	private val springBoot = listOf(
		"org.springframework.boot:spring-boot-starter-data-jpa",
		"org.springframework.boot:spring-boot-starter-web",
		"com.fasterxml.jackson.module:jackson-module-kotlin",
		"org.jetbrains.kotlin:kotlin-reflect"
	)

	private val swagger = listOf(
		"org.springdoc:springdoc-openapi-starter-webmvc-ui:${BuildConfig.Versions.OPENAPI_STARTER}"
	)

	private val jackson = listOf(
		"com.fasterxml.jackson.core:jackson-databind:${BuildConfig.Versions.JACKSON}",
		"com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${BuildConfig.Versions.JACKSON}"
	)

	val implementation = springBoot + swagger + jackson

	val runtimeOnly = listOf(
		"org.postgresql:postgresql",
		"org.springdoc:springdoc-openapi-kotlin:${BuildConfig.Versions.OPENAPI_KOTLIN}"
	)

	val jooqGenerator = listOf(
		"org.postgresql:postgresql:${BuildConfig.Versions.POSTGRESQL}"
	)

	val test = listOf(
		"org.springframework.boot:spring-boot-starter-test"
	)
}

dependencies {
	DependencyManagement.implementation.forEach { implementation(it) }
	DependencyManagement.runtimeOnly.forEach { runtimeOnly(it) }
	DependencyManagement.jooqGenerator.forEach { jooqGenerator(it) }
	DependencyManagement.test.forEach { testImplementation(it) }
}


// sourceSets 설정 다음 yml 파일 읽기
val yaml = Yaml()
val config: Map<String, Any> = yaml.load(FileInputStream(File("security-module/account-bank/src/main/resources/application.yml")))
val spring = config["spring"] as? Map<String, Any>
val datasource = spring?.get("datasource") as? Map<String, Any>

jooq {
	version.set("3.18.4")
	configurations {
		create("main") {
			generateSchemaSourceOnCompilation.set(true)
			jooqConfiguration.apply {
				logging = Logging.WARN
				jdbc.apply {
					driver = datasource?.get("driver-class-name")?.toString() ?: "org.postgresql.Driver"
					url = datasource?.get("url")?.toString() ?: "jdbc:postgresql://localhost:5432/your_db"
					user = datasource?.get("username")?.toString() ?: "postgres"
					password = datasource?.get("password")?.toString() ?: "password"
				}
				generator.apply {
					name = "org.jooq.codegen.KotlinGenerator"
					database.apply {
						name = "org.jooq.meta.postgres.PostgresDatabase"
						inputSchema = "public"
					}
					generate.apply {
						isDeprecated = false
						isKotlinSetterJvmNameAnnotationsOnIsPrefix = true
						isPojosAsKotlinDataClasses = true
						isFluentSetters = true
					}
					target.apply {
						packageName = "book.account"
						directory = "build/generated-src/jooq/main"
					}
					strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
				}
			}
		}
	}
}

tasks.withType<KotlinCompile> {
	dependsOn("generateJooq") // build 될 때 jooq 스키마 생성
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = BuildConfig.Project.JVM_TARGET
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

tasks.named("bootRun") {
	dependsOn("generateJooq")
}

tasks.withType<Test> {
	useJUnitPlatform()
}