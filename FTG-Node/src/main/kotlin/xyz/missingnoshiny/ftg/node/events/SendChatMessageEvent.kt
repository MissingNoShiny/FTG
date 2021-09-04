package xyz.missingnoshiny.ftg.node.events

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent

@Serializable
data class SendChatMessageEvent(override val timestamp: Instant, val message: String) : IncomingEvent() {
    override suspend fun invoke(context: EventContext) {
        (context as GameRoomContext).let {
            if (message.startsWith("/")) {
                context.room.handleCommand(context.user, message)
            } else {
                context.room.sendChatMessage(context.user, message, Clock.System.now())
            }
        }
    }
}