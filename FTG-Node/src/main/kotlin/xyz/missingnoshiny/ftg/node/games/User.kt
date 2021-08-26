package xyz.missingnoshiny.ftg.node.games

import io.ktor.http.cio.websocket.*
import xyz.missingnoshiny.ftg.core.events.WebsocketSessionEventHandler
import xyz.missingnoshiny.ftg.node.events.GameRoomContext

class User(val id: Int, val username: String, room: Room, session: DefaultWebSocketSession) {

    val handler = WebsocketSessionEventHandler(GameRoomContext(room, this), session)
}
