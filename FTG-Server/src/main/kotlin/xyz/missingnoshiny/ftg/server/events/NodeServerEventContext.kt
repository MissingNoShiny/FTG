package xyz.missingnoshiny.ftg.server.events

import xyz.missingnoshiny.ftg.core.events.EventContext
import xyz.missingnoshiny.ftg.server.Node

class NodeServerEventContext(val node: Node): EventContext()