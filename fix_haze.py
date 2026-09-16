with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "haze =" not in content:
    content = content.replace("[versions]", "[versions]\nhaze = \"1.5.3\"")
    content = content.replace("[libraries]", "[libraries]\nhaze = { group = \"dev.chrisbanes.haze\", name = \"haze\", version.ref = \"haze\" }\nhaze-materials = { group = \"dev.chrisbanes.haze\", name = \"haze-materials\", version.ref = \"haze\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content2 = f.read()

if "libs.haze" not in content2:
    content2 = content2.replace("dependencies {", "dependencies {\n    implementation(libs.haze)\n    implementation(libs.haze.materials)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content2)
