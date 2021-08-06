package xyz.missingnoshiny.ftg.server.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction

object Users: IntIdTable() {
    enum class Type {
        LOCAL,
        EXTERNAL
    }

    val creationTime = datetime("creationTime").defaultExpression(CurrentDateTime())
    val type = enumeration("type", Type::class)
    val administrator = bool("administrator").default(false)
}

object Follows: Table() {
    val followingUserId = reference("followingUserId", Users)
    val followedUserId = reference("followedUserId", Users)
    override val primaryKey = PrimaryKey(followingUserId, followedUserId)
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<User>(Users)
    var creationTime    by Users.creationTime
    var type            by Users.type
    var administrator   by Users.administrator
    var followedBy      by User.via(Follows.followedUserId, Follows.followingUserId)
    var following       by User.via(Follows.followingUserId, Follows.followedUserId)
}

object LocalUsers: IdTable<Int>() {
    override val id = integer("id").entityId().references(Users.id)
    override val primaryKey = PrimaryKey(id)

    val username = varchar("username", 32).uniqueIndex()
    val password = binary("password", 40)
}

class LocalUser(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<LocalUser>(LocalUsers)
    var username by LocalUsers.username
    var password by LocalUsers.password
}

object ExternalUsers: IdTable<Int>() {
    enum class Provider {
        DISCORD,
        TWITCH
    }

    override val id = integer("id").entityId().references(Users.id)
    override val primaryKey = PrimaryKey(id)

    val provider = enumeration("provider", Provider::class)
    val providerUserId = integer("providerUserId")
    val username = varchar("username", 32)
}

class ExternalUser(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ExternalUser>(ExternalUsers)
    var provider        by ExternalUsers.provider
    var providerUserId  by ExternalUsers.providerUserId
    var username        by ExternalUsers.username
}

fun createMissingTables() {
    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Users,
            LocalUsers,
            ExternalUsers
        )
    }
}
