package xyz.missingnoshiny.ftg.node.events.boggle

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.core.events.IncomingEvent
import xyz.missingnoshiny.ftg.node.events.GameRoomContext
import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGame

@Serializable
data class BoggleGuessEvent(override val timestamp: Instant, val guess: String): IncomingEvent() {
    override suspend fun invoke(context: EventContext) {
        (context as GameRoomContext).let {
            if (it.room.game != null || it.room.game !is BoggleGame) return
            val game = it.room.game as BoggleGame
            game.makeGuess(it.user, guess)
        }
    }
}