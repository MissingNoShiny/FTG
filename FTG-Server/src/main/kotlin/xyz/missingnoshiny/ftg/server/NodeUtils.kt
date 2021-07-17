package xyz.missingnoshiny.ftg.server

fun MutableList<Node>.getLeastUsed() = this.filter { it.isReady && it.isAlive }.minByOrNull { it.weight }
