package com.aivya.schoolmanagement.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class TeacherSession(
    val token: String,
    val teacherId: String,
    val fullName: String,
    val email: String,
    val role: String,
)

/**
 * Stores the JWT and the signed-in teacher's profile in an
 * AES-256-GCM-encrypted preferences file (backed by the Android Keystore),
 * instead of plain SharedPreferences. The token never appears in logs.
 */
class TokenStore(context: Context) {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "aivya_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun save(session: TeacherSession) {
        prefs.edit()
            .putString(KEY_TOKEN, session.token)
            .putString(KEY_TEACHER_ID, session.teacherId)
            .putString(KEY_FULL_NAME, session.fullName)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_ROLE, session.role)
            .apply()
    }

    fun load(): TeacherSession? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val teacherId = prefs.getString(KEY_TEACHER_ID, null) ?: return null
        val fullName = prefs.getString(KEY_FULL_NAME, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val role = prefs.getString(KEY_ROLE, "TEACHER") ?: "TEACHER"
        return TeacherSession(token, teacherId, fullName, email, role)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_TEACHER_ID = "teacher_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}
