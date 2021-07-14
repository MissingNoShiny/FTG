package xyz.missingnoshiny.ftg.node

import java.util.concurrent.atomic.AtomicInteger

class Room {
    companion object {
        val counter = AtomicInteger(1)
    }

    val id = counter.getAndIncrement()
}