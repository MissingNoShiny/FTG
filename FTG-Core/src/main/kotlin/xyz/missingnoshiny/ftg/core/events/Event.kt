package xyz.missingnoshiny.ftg.core.events

import kotlinx.datetime.Instant

abstract class Event {
    abstract val timestamp: Instant
}
