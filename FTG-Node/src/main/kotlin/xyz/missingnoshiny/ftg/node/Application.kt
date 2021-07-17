package xyz.missingnoshiny.ftg.node

import com.typesafe.config.ConfigFactory
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.config.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.core.events.EmptyContext
import xyz.missingnoshiny.ftg.core.events.WebsocketSessionEventHandler
import xyz.missingnoshiny.ftg.node.events.NodeHeartbeatEvent
import xyz.missingnoshiny.ftg.node.events.NodeReadyEvent
import xyz.missingnoshiny.ftg.node.plugins.configureRouting
import xyz.missingnoshiny.ftg.node.plugins.configureSerialization
import kotlin.system.exitProcess

val rooms = mutableListOf<Room>()

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

/**
 * Please note that you can use any other name instead of *module*.
 * Also note that you can have more than one module in your application.
 * */
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    configureSerialization()
    configureRouting()

}

@Suppress("unused")
fun Application.test() {
    val client = HttpClient {
        install(WebSockets)
    }

    launch {
        client.webSocket(method = HttpMethod.Get, host = "127.0.0.1", port = 1308, path = "/") {
            val handler = WebsocketSessionEventHandler(EmptyContext(), this)

            val appConfig = HoconApplicationConfig(ConfigFactory.load())
            val apiAddress = "${appConfig.property("api.host").getString()}:${appConfig.property("api.port").getString()}"
            handler.emitEvent(NodeReadyEvent(apiAddress))

            while (true) {
                handler.emitEvent(NodeHeartbeatEvent(rooms.size))
                println("Ok ${rooms.size}")
                delay(5000)
            }
        }
    }
}
