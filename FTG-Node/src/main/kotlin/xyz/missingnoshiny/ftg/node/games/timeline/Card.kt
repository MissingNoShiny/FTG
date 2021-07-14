package xyz.missingnoshiny.ftg.node.games.timeline

class Card(val year: Int, val name: String): Comparable<Card> {
    override fun compareTo(other: Card): Int {
        return year - other.year
    }
}