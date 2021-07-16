package xyz.missingnoshiny.ftg.node

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import xyz.missingnoshiny.ftg.core.events.IncomingEvent
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.node.events.NodeHeartbeatEvent
import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import java.io.File

fun main() {
    val d = Dictionary.fromFile(File("dictionary.txt")) { true }
    val p = d.getLetterFrequencies()

}
