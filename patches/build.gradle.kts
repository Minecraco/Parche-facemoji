group = "app.parchefacemoji"

patches {
    about {
        name = "Parche Facemoji"
        description = "Parches en español para Facemoji AI Emoji Keyboard (com.simejikeyboard)"
        source = "https://github.com/Minecraco/Parche-facemoji"
        author = "Minecraco"
        contact = "https://github.com/Minecraco/Parche-facemoji/issues"
        website = "https://github.com/Minecraco/Parche-facemoji"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)

    // Solo para la prueba que aplica el bundle a una APK real (ver
    // patches/src/test/kotlin/verificar/AplicarParchesTest.kt). No entra en el .mpp.
    testImplementation(kotlin("test"))
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
