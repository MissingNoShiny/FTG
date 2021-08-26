package xyz.missingnoshiny.ftg.node.games

import kotlinx.coroutines.delay
import xyz.missingnoshiny.ftg.node.events.NodeRemoveRoomEvent
import xyz.missingnoshiny.ftg.node.games.util.LimitedQueue
import xyz.missingnoshiny.ftg.node.rooms
import xyz.missingnoshiny.ftg.node.serverConnectionHandler
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class Room(private val id: String) {

    val users = mutableListOf<User>()
    val game: Game? = null
    private val chatHistory = LimitedQueue<ChatMessage>(20)

    /**
     * Dereferences the room from the global room list after 60 seconds without any user connected.
     */
    suspend fun activityCheckLoop() {
        while (true) {
            if (users.isEmpty()) {
                var empty = true
                for (i in 1..60) {
                    delay(Duration.seconds(1))
                    if (users.isNotEmpty()) {
                        empty = false
                        break
                    }
                }
                if (empty) {
                    // 60 seconds with no players, destroy room
                    rooms.remove(id)
                    serverConnectionHandler?.emitEvent(NodeRemoveRoomEvent(id))
                    break
                }
            }
            delay(Duration.seconds(1))
        }
    }

    suspend fun sendChatMessage(sender: User, message: String) {
        users.forEach {

        }
    }

    suspend fun addUser(user: User) {
        users.forEach {

        }
        users += user

    }

    suspend fun removeUser(user: User) {
        users -= user
    }

    fun getChatHistory() = chatHistory.toList()
}