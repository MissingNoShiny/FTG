package xyz.missingnoshiny.ftg.node

import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGame
import xyz.missingnoshiny.ftg.node.games.boggle.BoggleGrid
import xyz.missingnoshiny.ftg.node.games.boggle.downloadBoggleDictionary
import java.nio.file.Path

fun main() {
    val dictionary = BoggleGame.frenchDictionary
    val grid = BoggleGrid(3, dictionary)
    println(grid)
    println(grid.solutions.toList())

}
