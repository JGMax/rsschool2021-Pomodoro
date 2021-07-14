package gortea.jgmax.pomodoro.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppPreferences(context: Context) {
    val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun <T> putList(key: String, data: List<T>) {
        val gson = Gson()
        val jsonStr = gson.toJson(data)
        set(key, jsonStr)
    }

    inline fun <reified T> getList(key: String): List<T>? {
        val jsonStr = preferences.getString(key, null) ?: return null
        val gson = Gson()
        val type = object : TypeToken<List<T?>?>() {}.type
        return gson.fromJson(jsonStr, type)
    }

    private fun set(key: String, str: String) {
        preferences.edit()
            .putString(key, str)
            .apply()
    }

    fun clearList(key: String) {
        preferences.edit()
            .putString(key, null)
            .apply()
    }

    private companion object {
        private const val PREFERENCES_NAME = "TIMERS_LIST"
    }
}