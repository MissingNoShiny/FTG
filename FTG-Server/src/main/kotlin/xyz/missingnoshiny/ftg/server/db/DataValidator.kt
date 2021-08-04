package xyz.missingnoshiny.ftg.server.db

import kotlinx.serialization.Serializable
import xyz.missingnoshiny.ftg.server.api.SignupRequest


class DataValidator private constructor() {

    @Serializable
    data class ConstraintViolation(val hint: String)

    companion object {
        private fun validateUsername(username: String): MutableList<ConstraintViolation> {
            val violations = mutableListOf<ConstraintViolation>()

            // Length constraints
            if (username.length < 2) {
                violations.add(ConstraintViolation("Username must contain at least 2 characters"))
            } else if (username.length > 32) {
                violations.add(ConstraintViolation("Username must contain at most 32 characters"))
            }

            return violations
        }

        private fun validateLocalUsername(username: String): MutableList<ConstraintViolation> {
            val violations = validateUsername(username)

            if (!LocalUser.find { LocalUsers.username eq username }.empty()) {
                violations.add(ConstraintViolation("Username already exists among local users"))
            }

            return violations
        }

        private fun validatePassword(password: String): MutableList<ConstraintViolation> {
            val violations = mutableListOf<ConstraintViolation>()

            // Length constraints
            if (password.length < 12) {
                violations.add(ConstraintViolation("Password must contain at least 12 characters"))
            } else if (password.length > 100) {
                violations.add(ConstraintViolation("Password must contain at most 100 characters"))
            }

            // At least one lowercase letter
            if (!Regex("[a-z]").containsMatchIn(password)) {
                violations.add(ConstraintViolation("Password must contain at least one lowercase letter"))
            }
            // At least one uppercase letter
            if (!Regex("[A-Z]").containsMatchIn(password)) {
                violations.add(ConstraintViolation("Password must contain at least one uppercase letter"))
            }
            // At least one digit
            if (!password.any { it.isDigit() }) {
                violations.add(ConstraintViolation("Password must contain at least one digit"))
            }

            return violations
        }

        fun validateSignupRequest(data: SignupRequest): List<ConstraintViolation> {
            return validateLocalUsername(data.username) + validatePassword(data.password)
        }
    }
}