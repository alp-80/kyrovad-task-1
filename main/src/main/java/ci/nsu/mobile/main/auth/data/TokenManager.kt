package ci.nsu.mobile.main.auth.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Base64

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("token", null)
        set(value) {
            prefs.edit().putString("token", value).apply()
            if (value == null) {
                _userId.value = null
                prefs.edit().remove("userId").apply()
            } else {
                val userId = extractUserIdFromToken(value)
                _userId.value = userId
                if (userId != null) {
                    prefs.edit().putLong("userId", userId).apply()
                }
            }
        }

    private val _userId = MutableStateFlow<Long?>(prefs.getLong("userId", -1).takeIf { it != -1L })
    val userId: StateFlow<Long?> = _userId.asStateFlow()

    private fun extractUserIdFromToken(token: String): Long? {
        return try {
            val decoded = String(Base64.getDecoder().decode(token))
            val userIdPattern = Regex("\"userId\":(\\d+)")
            val match = userIdPattern.find(decoded)
            match?.groupValues?.get(1)?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun getUserId(): Long? = _userId.value

    fun clear() {
        prefs.edit().clear().apply()
        _userId.value = null
    }
}