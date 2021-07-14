package xyz.missingnoshiny.ftg.core.events

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class WebsocketSessionEventHandler(private val context: EventContext, private val session: DefaultWebSocketSession) {
    private val eventMap = HashMap<String, KClass<out IncomingEvent>>()

    fun registerIncomingEvent(eventClass: KClass<out IncomingEvent>) = eventMap.put(eventClass.simpleName.toString(), eventClass)

    suspend fun emitEvent(outgoingEvent: OutgoingEvent) {
        val data = "${outgoingEvent::class.simpleName.toString()}|${Json { encodeDefaults = true }.encodeToString(serializer(outgoingEvent::class.createType()), outgoingEvent)}"
        println(data)
        session.send(data)
    }

    private fun handleIncomingEvent(eventString: String) {
        println(eventString)
        val (eventName, json) = eventString.split("|", limit = 2)
        val cls = eventMap[eventName] ?: throw Exception("Invalid event")
        val event = Json.decodeFromString(serializer(cls.createType()), json) as IncomingEvent
        event.invoke(context)
    }

    suspend fun handleIncomingEvents() {
        for (frame in session.incoming) {
            frame as? Frame.Text ?: continue
            try {
                handleIncomingEvent(frame.readText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}