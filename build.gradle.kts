plugins {
    kotlin("jvm") version "1.9.24"
    application
}
repositories {
    mavenCentral()
    maven { url = uri("https://repo1.maven.org/maven2") }
}
dependencies {

    implementation("com.github.pengrad:java-telegram-bot-api:6.9.1")
}
application { mainClass.set("BotMainKt") }
