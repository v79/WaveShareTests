plugins {
	kotlin("multiplatform") version "1.4.20"
}

group = "org.liamjd.pi"
version = "1.1-SNAPSHOT"

repositories {
	mavenCentral()
	jcenter()
}


kotlin {
	val hostOs = System.getProperty("os.name")
	val isMingwX64 = hostOs.startsWith("Windows")




	linuxArm32Hfp("Pi") {

		compilations {
			"main" {
				cinterops {
					val libbcm by cinterops.creating {}
					val libbmp by cinterops.creating {}
					val libcurl by cinterops.creating {
						includeDirs("src/include/curl")
					}
				}

			}
		}
		binaries {
/*             staticLib {
				 this.optimized = false
			 }*/
			executable {
				this.optimized = false
				entryPoint = "org.liamjd.pi.main"
			}
		}
	}

	sourceSets {
		val PiMain by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-io:0.1.16")
			}
		}
	}

}



tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
	kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
