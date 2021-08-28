package xyz.missingnoshiny.ftg.core.events

import kotlinx.serialization.Serializable

@Serializable
abstract class IncomingEvent: Event() {
    abstract suspend fun invoke(context: EventContext)
}