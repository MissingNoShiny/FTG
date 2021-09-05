package xyz.missingnoshiny.ftg.node.games

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import xyz.missingnoshiny.ftg.core.RoomSerializable
import xyz.missingnoshiny.ftg.core.RoomState
import xyz.missingnoshiny.ftg.core.events.OutgoingEvent
import xyz.missingnoshiny.ftg.node.events.*
import xyz.missingnoshiny.ftg.node.events.boggle.BoggleGuessEvent
import xyz.missingnoshiny.ftg.node.events.boggle.BoggleStartEvent
import xyz.missingnoshiny.ftg.node.events.server.NodeRemoveRoomEvent
import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGame
import xyz.missingnoshiny.ftg.node.games.util.LimitedQueue
import xyz.missingnoshiny.ftg.node.rooms
import xyz.missingnoshiny.ftg.node.serializable.ChatMessage
import xyz.missingnoshiny.ftg.node.serverConnectionHandler
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class Room(private val id: String, private val isPublic: Boolean) {

    val users = mutableListOf<User>()

    var game: Game? = null
    val players = mutableListOf<User>()

    var state: RoomState = RoomState.NO_GAME
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

    suspend fun sendChatMessage(sender: User, message: String, timestamp: Instant) {
        println("chat message")
        val author = sender.toSerializable()
        broadcastEventToRoom(ChatMessageEvent(ChatMessage(author, message, timestamp)))
    }

    suspend fun addUser(user: User) {
        users += user
        user.handler.registerIncomingEvent(SendChatMessageEvent::class)

        val userList = users.map { it.toSerializable() }
        broadcastEventToRoom(AddRoomUserEvent(user.toSerializable(), userList))
    }

    suspend fun removeUser(user: User) {
        users -= user
        val userList = users.map { it.toSerializable() }
        broadcastEventToRoom(RemoveRoomUserEvent(user.toSerializable(), userList))
    }

    suspend fun broadcastEventToRoom(event: OutgoingEvent) = coroutineScope {
        users.forEach {
            launch { it.handler.emitEvent(event) }
        }
    }

    suspend fun broadcastEventToPlayers(event: OutgoingEvent) = coroutineScope {
        players.forEach {
            launch { it.handler.emitEvent(event) }
        }
    }

    suspend fun broadcastEventToNonPlayers(event: OutgoingEvent) = coroutineScope {
        users.filter { it !in players }.forEach {
            launch { it.handler.emitEvent(event) }
        }
    }

    suspend fun broadcastEventToRoomExceptFor(user: User, event: OutgoingEvent) = coroutineScope {
        users.filter { it != user }.forEach {
            launch {  it.handler.emitEvent(event) }
        }
    }

    private suspend fun prepareGame() {
        state = RoomState.PRE
        players.clear()
        broadcastEventToRoom(PrepareGameEvent())
    }

    private suspend fun startGame() {
        game = BoggleGame(players, this)
        state = RoomState.PLAYING
        (game as BoggleGame).let {
            broadcastEventToPlayers(BoggleStartEvent(it.getGrid(), it.getMaxScore(), it.getWordCount(), 180, true))
            broadcastEventToNonPlayers(BoggleStartEvent(it.getGrid(), it.getMaxScore(), it.getWordCount(), 180, false))
        }

    }

    private fun stopGame() {
        (game as BoggleGame).timeLeft = 0
    }

    suspend fun handleCommand(sender: User, command: String) {
        when (command.removePrefix("/")) {
            "join" -> {
                if (state != RoomState.PRE) {
                    return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
                }
                if (sender in players) return
                players += sender
                broadcastEventToRoom(AddPlayerEvent(sender.toSerializable()))
            }
            "leave" -> {
                if (state != RoomState.PRE) {
                    return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
                }
                if (sender !in players) return
                players -= sender
                broadcastEventToRoom(RemovePlayerEvent(sender.toSerializable()))
            }
            "boggle" -> {
                if (!isAllowedGameCommands(sender)) {
                    return sender.handler.emitEvent(InvalidCommandEvent.NO_PERMISSION)
                }
                if (state != RoomState.NO_GAME && state != RoomState.POST) {
                    return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
                }
                prepareGame()
            }
            "start" -> {
                if (!isAllowedGameCommands(sender)) {
                    return sender.handler.emitEvent(InvalidCommandEvent.NO_PERMISSION)
                }
                if (state != RoomState.PRE) {
                    return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
                }
                startGame()
            }
            "stop" -> {
                if (!isAllowedGameCommands(sender)) {
                    return sender.handler.emitEvent(InvalidCommandEvent.NO_PERMISSION)
                }
                if (state != RoomState.PLAYING) {
                    return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
                }
                stopGame()
            }
            else -> {
                return sender.handler.emitEvent(InvalidCommandEvent.INVALID)
            }
        }
    }

    private fun isAllowedGameCommands(user: User) = user.administrator || users.indexOf(user) == 0

    fun getChatHistory() = chatHistory.toList()

    fun toSerializable(): RoomSerializable {
        return RoomSerializable(
            id,
            users.map { it.toSerializable() },
            isPublic,
            state
        )
    }

    fun hasUser(id: Int) = users.any { it.id == id }
}