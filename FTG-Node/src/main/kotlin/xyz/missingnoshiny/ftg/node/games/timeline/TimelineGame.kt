package xyz.missingnoshiny.ftg.node.games.timeline

import xyz.missingnoshiny.ftg.node.games.Game

class TimelineGame: Game() {
    private fun List<Card>.isSorted() = this.zipWithNext { a, b -> a <= b }.all { it }
}