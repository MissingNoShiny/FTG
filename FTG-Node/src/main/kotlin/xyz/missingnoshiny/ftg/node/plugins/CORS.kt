package xyz.missingnoshiny.ftg.node.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureCORS() {
    install(CORS) {
        anyHost()
    }
}