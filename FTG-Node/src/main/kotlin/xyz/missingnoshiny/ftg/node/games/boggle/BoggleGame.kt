package xyz.missingnoshiny.ftg.node.games.boggle

import xyz.missingnoshiny.ftg.node.events.boggle.BoggleCorrectGuessEvent
import xyz.missingnoshiny.ftg.node.events.boggle.BoggleUpdateScoreEvent
import xyz.missingnoshiny.ftg.node.events.boggle.BoggleWrongGuessEvent
import xyz.missingnoshiny.ftg.node.games.Game
import xyz.missingnoshiny.ftg.node.games.User
import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import xyz.missingnoshiny.ftg.node.games.util.Trie
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class BoggleGame(players: List<User>, roomMembers: MutableList<User>) : Game(players, roomMembers) {
    companion object {
        val frenchDictionary = Dictionary.fromFile(File("dictionary.txt")) { true }
    }

    private val grid = BoggleGrid(4, frenchDictionary)

    private val scores = players.associateWith { AtomicInteger(0) }
    private val guesses = players.associateWith { Trie() }

    suspend fun makeGuess(player: User, guess: String) {
        if (guess in grid.solutions) {
            guesses[player]?.insert(guess) ?: return
            val newScore = scores[player]!!.addAndGet(getScore(guess))
            player.handler.emitEvent(BoggleCorrectGuessEvent(guess, newScore))
            broadcastEventToRoomExceptFor(player, BoggleUpdateScoreEvent(player.id, newScore))
        } else {
            player.handler.emitEvent(BoggleWrongGuessEvent(guess))
        }
    }
}