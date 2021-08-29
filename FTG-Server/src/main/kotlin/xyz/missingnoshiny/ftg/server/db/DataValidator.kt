package xyz.missingnoshiny.ftg.server.db

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.missingnoshiny.ftg.server.api.SignupRequest


class DataValidator private constructor() {

    @Serializable
    data class ConstraintViolation(val hint: String)

    companion object {
        private fun validateUsername(username: String): MutableList<ConstraintViolation> {
            val violations = mutableListOf<ConstraintViolation>()

            // Length constraints
            if (username.length < 2) {
                violations.add(ConstraintViolation("Le nom d'utilisateur doit comporter au moins 2 caractères."))
            } else if (username.length > 32) {
                violations.add(ConstraintViolation("Le nom d'utilisateur doit comporter au plus 32 caractères."))
            }

            return violations
        }

        private fun validateLocalUsername(username: String): MutableList<ConstraintViolation> {
            val violations = validateUsername(username)

            transaction {
                if (!LocalUser.find { LocalUsers.username eq username }.empty()) {
                    violations.add(ConstraintViolation("Ce nom d'utilisateur est déjà pris par un compte local."))
                }
            }

            return violations
        }

        private fun validatePassword(password: String): MutableList<ConstraintViolation> {
            val violations = mutableListOf<ConstraintViolation>()

            // Length constraints
            if (password.length < 12) {
                violations.add(ConstraintViolation("Le mot de passe doit contenir au moins 12 caractères."))
            } else if (password.length > 100) {
                violations.add(ConstraintViolation("Le mot de passe doit contenir au plus 12 caractères."))
            }

            // At least one lowercase letter
            if (!Regex("[a-z]").containsMatchIn(password)) {
                violations.add(ConstraintViolation("Le mot de passe doit contenir une lettre minuscule."))
            }
            // At least one uppercase letter
            if (!Regex("[A-Z]").containsMatchIn(password)) {
                violations.add(ConstraintViolation("Le mot de passe doit contenir une lettre majuscule."))
            }
            // At least one digit
            if (!password.any { it.isDigit() }) {
                violations.add(ConstraintViolation("Le mot de passe doit contenir un chiffre."))
            }

            return violations
        }

        fun validateSignupRequest(data: SignupRequest): List<ConstraintViolation> {
            return validateLocalUsername(data.username) + validatePassword(data.password)
        }
    }
}