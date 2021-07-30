package xyz.missingnoshiny.ftg.server.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.`java-time`.CurrentDateTime
import org.jetbrains.exposed.sql.`java-time`.datetime

object Users: IntIdTable() {
    val creationTime = datetime("creationTime").defaultExpression(CurrentDateTime())
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<User>(Users)
    var creationTime by Users.creationTime
}

object LocalUsers: IntIdTable() {
    override val id = integer("id").entityId().references(Users.id)
    val username = varchar("username", 32).uniqueIndex()
    val password = binary("password", 40)

    override val primaryKey = PrimaryKey(id)
}

class LocalUser(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<LocalUser>(LocalUsers)
    var username by LocalUsers.username
    var password by LocalUsers.password
}

object ExternalUsers: IntIdTable() {
    enum class Provider {
        DISCORD,
        TWITCH
    }

    override val id = integer("id").entityId().references(Users.id)
    val provider = enumeration("provider", Provider::class)
    val providerUserId = integer("providerUserId")
}

class ExternalUser(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<ExternalUser>(ExternalUsers)
    var provider by ExternalUsers.provider
    var providerUserId by ExternalUsers.providerUserId
}
