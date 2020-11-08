plugins {
    kotlin("multiplatform") version "1.4.10"
}

group = "org.liamjd.pi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")

    linuxArm32Hfp("Pi") {
        compilations {
            "main" {
                cinterops {
                    val libbcm by cinterops.creating {}
                }
            }
        }
        binaries {
            executable("waveshare") {
                entryPoint = "org.liamjd.pi.main"
            }
        }
    }


    sourceSets {
       val PiMain by getting
    }


}
