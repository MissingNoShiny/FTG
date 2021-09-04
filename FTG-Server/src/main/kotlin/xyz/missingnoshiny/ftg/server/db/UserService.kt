package xyz.missingnoshiny.ftg.server.db

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {
    @Serializable
    data class UserInfo(val id: Int, val username: String, val administrator: Boolean, val type: Users.Type, val provider: ExternalUsers.Provider? = null)

    companion object {
        fun getUserInfos(user: User): UserInfo = when (user.type) {
            Users.Type.LOCAL -> {
                val localUser = transaction {
                    LocalUser[user.id]
                }
                UserInfo(localUser.id.value, localUser.username, user.administrator, user.type)
            }
            Users.Type.EXTERNAL -> {
                val externalUser = transaction {
                    ExternalUser[user.id]
                }
                UserInfo(externalUser.id.value, externalUser.username, user.administrator, user.type, externalUser.provider)
            }
        }

        fun findById(id: Int) = transaction {
            User.findById(id)
        }

        fun isAdministrator(id: Int) = findById(id)?.administrator

        fun getUsernameFromId(id: Int) = findById(id)

        fun follow(followerId: Int, followedId: Int) {
            transaction {
                val followingUser = findById(followedId)
                val followedUser = findById(followedId)
                if (followingUser != null && followedUser != null) {
                    val current = followingUser.following
                    followingUser.following = SizedCollection(current + followedUser)
                }
            }
        }

        fun unfollow(followerId: Int, followedId: Int) {
            transaction {
                val followingUser = findById(followedId)
                val followedUser = findById(followedId)
                if (followingUser != null && followedUser != null) {
                    val current = followingUser.following
                    followingUser.following = SizedCollection(current - followedUser)
                }
            }
        }
    }
}