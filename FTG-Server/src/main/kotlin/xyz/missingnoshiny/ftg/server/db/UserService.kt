package xyz.missingnoshiny.ftg.server.db

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {
    @Serializable
    data class UserInfo(val username: String, val type: Users.Type)

    companion object {
        fun getUserInfos(user: User): UserInfo = when (user.type) {
            Users.Type.LOCAL -> {
                val localUser = transaction {
                    LocalUser[user.id]
                }
                UserInfo(localUser.username, user.type)
            }
            Users.Type.EXTERNAL -> {
                val externalUser = transaction {
                    ExternalUser[user.id]
                }
                UserInfo(externalUser.username, user.type)
            }
        }

        fun findById(id: Int) = transaction {
            User.findById(id)
        }

        fun getUsernameFromId(id: Int) = findById(id)
    }
}