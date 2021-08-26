package xyz.missingnoshiny.ftg.node.events

import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.games.User

class GameRoomContext(val room: Room, val user: User): EventContext()