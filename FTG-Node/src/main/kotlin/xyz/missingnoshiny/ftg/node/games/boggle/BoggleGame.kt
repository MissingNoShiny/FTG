package xyz.missingnoshiny.ftg.node.games.boggle

import xyz.missingnoshiny.ftg.node.games.Game
import xyz.missingnoshiny.ftg.node.games.util.Dictionary
import java.io.File

class BoggleGame : Game() {
    companion object {
        val frenchDictionary = Dictionary.fromFile(File("dictionary.txt")) { true }
    }
}