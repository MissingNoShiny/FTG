package xyz.missingnoshiny.ftg.server

fun MutableList<Node>.getLeastUsed() = this.minByOrNull { it.weight }
