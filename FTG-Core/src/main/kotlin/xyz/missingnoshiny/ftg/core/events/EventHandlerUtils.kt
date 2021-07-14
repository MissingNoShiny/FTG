package xyz.missingnoshiny.ftg.core.events

suspend fun MutableList<WebsocketSessionEventHandler>.broadcast(outgoingEvent: OutgoingEvent) =
    this.forEach { it.emitEvent(outgoingEvent) }