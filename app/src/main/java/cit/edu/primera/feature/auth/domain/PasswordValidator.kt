package cit.edu.primera.feature.auth.domain

import cit.edu.primera.feature.auth.ui.PasswordStrength

enum class PasswordRuleViolation {
    TOO_SHORT,
    MISSING_LETTER,
    MISSING_DIGIT_OR_SYMBOL
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val violations: List<PasswordRuleViolation> = emptyList()
)

object PasswordValidator {
    const val MIN_PASSWORD_LENGTH = 8

    fun validate(password: String): PasswordValidationResult {
        if (password.isBlank()) {
            return PasswordValidationResult(
                isValid = false,
                violations = listOf(PasswordRuleViolation.TOO_SHORT)
            )
        }

        val violations = mutableListOf<PasswordRuleViolation>()

        if (password.length < MIN_PASSWORD_LENGTH) {
            violations.add(PasswordRuleViolation.TOO_SHORT)
        }
        if (!password.any { it.isLetter() }) {
            violations.add(PasswordRuleViolation.MISSING_LETTER)
        }
        if (!password.any { it.isDigit() || !it.isLetterOrDigit() }) {
            violations.add(PasswordRuleViolation.MISSING_DIGIT_OR_SYMBOL)
        }

        return PasswordValidationResult(
            isValid = violations.isEmpty(),
            violations = violations
        )
    }

    fun calculateStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        if (password.length < 6) return PasswordStrength.WEAK

        var score = 0
        if (password.length >= MIN_PASSWORD_LENGTH) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 3 && password.length >= MIN_PASSWORD_LENGTH -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.FAIR
            else -> PasswordStrength.WEAK
        }
    }
}
