package cit.edu.primera.feature.dashboard.data

import java.util.Date

data class UserDto(
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val fullName: String? = null,
    val birthday: Date? = null,
    val dueDate: Date? = null,
    val steps: Long? = null,
    val stepsGoal: Long? = null,
    val heartRateBpm: Long? = null,
    val sleepHours: Long? = null,
    val sleepMinutes: Long? = null,
    val spO2: Long? = null,
    val heightCm: Long? = null
)
