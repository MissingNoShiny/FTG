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
import xyz.missingnoshiny.ftg.node.events.server.NodeHeartbeatEvent
import xyz.missingnoshiny.ftg.node.events.server.NodeReadyEvent
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGame
import xyz.missingnoshiny.ftg.node.plugins.*
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
    configureCORS()
    configureRouting()
    configureSockets()
}

@Suppress("unused")
fun Application.test() {
    val client = HttpClient {
        install(WebSockets)
    }
    launch {
        val dictionary = BoggleGame.frenchDictionary
        println(dictionary.getLetterFrequencies())
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
                serverConnectionHandler!!.emitEvent(NodeHeartbeatEvent(rooms.mapValues { it.value.toSerializable() }))
                println("Ok ${rooms.size}")
                delay(1000)
            }
        }
        exitProcess(-1)
    }
}
