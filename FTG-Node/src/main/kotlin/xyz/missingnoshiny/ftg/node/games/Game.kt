package xyz.missingnoshiny.ftg.node.games

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent

abstract class Game(val players: List<User>, val room: Room) {

    fun disconnectRoomMember(user: User) {
        if (user in players) return //TODO Find out what to do in this situation
    }
}