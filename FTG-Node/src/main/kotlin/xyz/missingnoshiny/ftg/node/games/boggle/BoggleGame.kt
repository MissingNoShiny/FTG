package xyz.missingnoshiny.ftg.node.games.boggle

import kotlinx.coroutines.*
import xyz.missingnoshiny.ftg.core.RoomState
import xyz.missingnoshiny.ftg.node.events.boggle.*
import xyz.missingnoshiny.ftg.node.games.Game
import xyz.missingnoshiny.ftg.node.games.Room
import xyz.missingnoshiny.ftg.node.games.User
import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import xyz.missingnoshiny.ftg.node.games.util.Trie
import xyz.missingnoshiny.ftg.node.games.util.normalize
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

@OptIn(DelicateCoroutinesApi::class)
class BoggleGame(players: List<User>, room: Room) : Game(players, room) {
    companion object {
        val frenchDictionary = Dictionary.fromStream(ClassLoader.getSystemResourceAsStream("dictionary.txt")!!) { true }
    }

    private val grid = BoggleGrid(4, frenchDictionary)

    private val scores = players.associateWith { AtomicInteger(0) }
    private val guesses = players.associateWith { Trie() }

    var timeLeft: Int = 180

    init {
        players.forEach {
            it.handler.registerIncomingEvent(BoggleGuessEvent::class)
        }
        GlobalScope.launch { start() }
    }

    suspend fun start() {
        while (true) {
            delay(1000)
            timeLeft--
            room.broadcastEventToRoom(BoggleUpdateTimerEvent(timeLeft))
            if (timeLeft <= 0) {
                stop()
                break
            }
        }
        println("Fini")
    }

    suspend fun stop() {
        val solutions = grid.solutions.map { Guess(it.normalize().uppercase(), it) }
        room.broadcastEventToRoom(BoggleStopEvent(solutions))
        players.forEach {
            it.handler.deregisterIncomingEvent(BoggleGuessEvent::class)
        }
        room.state = RoomState.POST
    }

    suspend fun makeGuess(player: User, guess: String) {
        if (player !in players) return
        if (guess.normalize().uppercase() != guess) return player.handler.emitEvent(BoggleWrongGuessEvent(guess, GuessStatus.INVALID))

        val word = grid.solutions.getFromKey(guess.lowercase())
        if (word != null) {
            if (guess in guesses[player]!!) {
                player.handler.emitEvent(BoggleWrongGuessEvent(guess, GuessStatus.ALREADY_GUESSED))
            }

            guesses[player]!!.insert(word)
            player.handler.emitEvent(BoggleCorrectGuessEvent(Guess(guess, word)))

            val newScore = scores[player]!!.addAndGet(getScore(guess))
            room.broadcastEventToRoom(BoggleUpdateScoreEvent(player.id, newScore))
        } else {
            player.handler.emitEvent(BoggleWrongGuessEvent(guess, GuessStatus.INVALID))
        }
    }

    fun getGrid(): List<List<Char>> = grid.getGrid()

    fun getMaxScore(): Int = grid.maxScore

    fun getWordCount(): Int = grid.solutions.toList().size
}