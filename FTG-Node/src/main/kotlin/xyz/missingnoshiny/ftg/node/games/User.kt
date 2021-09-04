package xyz.missingnoshiny.ftg.node.games

import io.ktor.http.cio.websocket.*
import xyz.missingnoshiny.ftg.core.events.WebsocketSessionEventHandler
import xyz.missingnoshiny.ftg.node.events.GameRoomContext
import xyz.missingnoshiny.ftg.core.UserSerializable

class User(val id: Int, val username: String, val administrator: Boolean, room: Room, session: DefaultWebSocketSession) {

    val handler = WebsocketSessionEventHandler(GameRoomContext(room, this), session)

    fun toSerializable(): UserSerializable = UserSerializable(id, username, administrator)
}
