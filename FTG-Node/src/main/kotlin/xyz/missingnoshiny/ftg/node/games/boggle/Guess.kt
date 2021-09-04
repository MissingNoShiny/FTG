package xyz.missingnoshiny.ftg.node.games.boggle

import kotlinx.serialization.Serializable

@Serializable
data class Guess(val normalized: String, val value: String)