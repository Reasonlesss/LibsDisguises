plugins {
    `java-library`
}

apply(from = rootProject.file("nms/nmsModule.gradle"))

extra["craftbukkitVersion"] = "1.21.5-R0.1-SNAPSHOT";

description = "v1_21_R4"

dependencies {
    compileOnly(libs.com.mojang.authlib.new)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}