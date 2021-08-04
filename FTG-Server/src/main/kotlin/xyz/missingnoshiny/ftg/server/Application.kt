package xyz.missingnoshiny.ftg.server

import io.ktor.application.*
import xyz.missingnoshiny.ftg.server.db.DatabaseInitializer
import xyz.missingnoshiny.ftg.server.plugins.auth.configureAuthentication
import xyz.missingnoshiny.ftg.server.plugins.configureCORS
import xyz.missingnoshiny.ftg.server.plugins.configureRouting
import xyz.missingnoshiny.ftg.server.plugins.configureSerialization
import xyz.missingnoshiny.ftg.server.plugins.configureSockets

val nodes = mutableListOf<Node>()
val rooms = HashMap<String, Node>()

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    DatabaseInitializer
    configureSerialization()
    configureRouting()
    configureSockets()
    configureCORS()
    configureAuthentication()
}
