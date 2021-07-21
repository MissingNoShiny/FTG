package xyz.missingnoshiny.ftg.node

import kotlinx.coroutines.delay
import xyz.missingnoshiny.ftg.node.events.NodeRemoveRoomEvent
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class Room(private val id: String) {

    val players = mutableListOf<Player>()

    /**
     * Dereferences the room from the global room list after 60 seconds without any player connected.
     */
    suspend fun activityCheckLoop() {
        while (true) {
            if (players.isEmpty())  {
                for (i in 1..60) {
                    delay(Duration.Companion.seconds(1))
                    if (players.isNotEmpty()) break
                }
                // 60 seconds with no players, destroy room
                rooms.remove(id)
                serverConnectionHandler?.emitEvent(NodeRemoveRoomEvent(id))
                break
            }
            delay(Duration.Companion.seconds(1))
        }
    }
}