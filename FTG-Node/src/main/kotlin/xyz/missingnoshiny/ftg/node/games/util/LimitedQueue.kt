package xyz.missingnoshiny.ftg.node.games.util

import java.util.*

class LimitedQueue<E>(val limit: Int): LinkedList<E>() {
    override fun push(element: E) {
        super.push(element)
        if (size > limit) removeLast()
    }
}