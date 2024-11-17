import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
// Configuration to read the YAML file in Gradle
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
// Jooq Logging settings
import org.jooq.meta.jaxb.Logging

// The functionality operates at Runtime, not Compiletime, so dependencies must be added to the buildscript
// The class path needs to be configured during the initial Gradle setup stage, so the buildscript block should be placed at the top of the script file
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

// Plugin Configuration
plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.2"

	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"

	id("nu.studer.jooq") version "8.2"
}

// project settings
group = BuildConfig.Project.GROUP
version = BuildConfig.Project.VERSION
java { sourceCompatibility = JavaVersion.VERSION_17 }
configurations { compileOnly { extendsFrom(configurations.annotationProcessor.get()) } }

// Resource directory paths must match the main project structure for proper YAML configuration injection at runtime
sourceSets {
	main {
		resources {
			srcDir("src/main/resources")
			srcDir("security-module/account-bank/src/main/resources")
		}
	}
}

// Configures the Maven Central repository as the primary source for downloading dependencies
repositories { mavenCentral() }

// Declares the external libraries and frameworks required by the project
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

// YAML configuration file path
internal val applicationYamlPath = BuildConfig.Paths.CONFIG_PATH

// Classes for application and JOOQ configuration
class AppConfig {
	var dataSourceConfig: DataSourceConfig = DataSourceConfig()
	var jpaConfig: JpaConfig = JpaConfig()
}

class DataSourceConfig {
	var driverClassName: String = ""
	var url: String = ""
	var username: String = ""
	var password: String = ""
}

class JpaConfig {
	var properties: Properties = Properties()
	var hibernate: Hibernate = Hibernate()
	var showSql: Boolean = false

	class Properties {
		var hibernateProperties: HibernateProperties = HibernateProperties()
	}

	class HibernateProperties {
		var dialect: String = ""
	}

	class Hibernate {
		var ddlAuto: String = ""
	}
}

class JooqGeneratorConfig {
	val inputSchema: String = "public"
	val packageName: String = "book.account"
	val directory: String = "${project.buildDir}/generated-src/jooq/main"
}

// Read YAML configuration file
fun readYamlConfig(): Map<*, *>? {
	val file = File(applicationYamlPath)
	val mapper = ObjectMapper(YAMLFactory())
	return mapper.readValue(file, Map::class.java)
}

// Configure JOOQ settings
fun configureJooq(config: AppConfig, generatorConfig: JooqGeneratorConfig) {
	jooq {
		version.set("3.18.4")
		configurations {
			create("main") {
				generateSchemaSourceOnCompilation.set(true)
				jooqConfiguration.apply {
					logging = Logging.WARN
					jdbc.apply {
						driver = config.dataSourceConfig.driverClassName
						url = config.dataSourceConfig.url
						user = config.dataSourceConfig.username
						password = config.dataSourceConfig.password
					}
					generator.apply {
						name = "org.jooq.codegen.KotlinGenerator"
						database.apply {
							name = "org.jooq.meta.postgres.PostgresDatabase"
							inputSchema = generatorConfig.inputSchema
						}
						generate.apply {
							isDeprecated = false
							isKotlinSetterJvmNameAnnotationsOnIsPrefix = true
							isPojosAsKotlinDataClasses = true
							isFluentSetters = true
						}
						target.apply {
							packageName = generatorConfig.packageName
							directory = generatorConfig.directory
						}
						strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
					}
				}
			}
		}
	}
}

// Load YAML configuration and configure JOOQ
val config = readYamlConfig()
val spring = config?.get("spring") as? Map<String, Any>
val datasource = spring?.get("datasource") as? Map<String, Any>

val appConfig = AppConfig()
val jooqConfig = JooqGeneratorConfig()
configureJooq(appConfig, jooqConfig)

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
		val config = readYamlConfig()
		println(config)
	}
}

tasks.named("bootRun") {
	dependsOn("generateJooq")
}

tasks.withType<Test> {
	useJUnitPlatform()
}