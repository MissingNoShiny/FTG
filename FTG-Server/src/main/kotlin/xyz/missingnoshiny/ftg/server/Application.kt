package xyz.missingnoshiny.ftg.server

import io.ktor.application.*
import xyz.missingnoshiny.ftg.server.plugins.*

val nodes = mutableListOf<Node>()

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more then one modules in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    configureRouting()
    configureSockets()
}
