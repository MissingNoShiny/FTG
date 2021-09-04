package xyz.missingnoshiny.ftg.node.games.timeline

import xyz.missingnoshiny.ftg.node.games.Game
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.games.User

class TimelineGame(players: List<User>, room: Room): Game(players, room) {
    private fun List<Card>.isSorted() = this.zipWithNext { a, b -> a <= b }.all { it }
}