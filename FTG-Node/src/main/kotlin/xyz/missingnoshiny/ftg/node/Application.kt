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
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.plugins.configureAuthentication
import xyz.missingnoshiny.ftg.node.plugins.configureCORS
import xyz.missingnoshiny.ftg.node.plugins.configureRouting
import xyz.missingnoshiny.ftg.node.plugins.configureSerialization
import kotlin.system.exitProcess

val rooms = HashMap<String, Room>()
// TODO: Figure out a better way to allow rooms ro remove themselves
var serverConnectionHandler: WebsocketSessionEventHandler? = null

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    configureSerialization()
    configureAuthentication()
    configureRouting()
    configureCORS()
}

@Suppress("unused")
fun Application.test() {
    val client = HttpClient {
        install(WebSockets)
    }

    launch {
        val remoteHost = environment.config.property("remote.host").getString()
        val remotePort = environment.config.property("remote.port").getString().toInt()
        println("$remoteHost:$remotePort")
        client.wss(method = HttpMethod.Get, host = remoteHost, port = remotePort, path = "/") {
            serverConnectionHandler = WebsocketSessionEventHandler(EmptyContext(), this)

            val appConfig = HoconApplicationConfig(ConfigFactory.load())
            val apiAddress = "${appConfig.property("api.host").getString()}:${appConfig.property("api.port").getString()}"
            serverConnectionHandler!!.emitEvent(NodeReadyEvent(apiAddress))

            while (true) {
                if (!serverConnectionHandler!!.connected) break
                serverConnectionHandler!!.emitEvent(NodeHeartbeatEvent(rooms.mapValues { it.value.users.map { user -> user.id }}))
                println("Ok ${rooms.size}")
                delay(5000)
            }
        }
        exitProcess(-1)
    }
}
