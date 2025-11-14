package com.example.myactivitylauncher.data

import android.content.Context

class FavoriteManager private constructor(context: Context) {

    companion object {
        private const val PREF_NAME = "favorite_manager"
        private const val KEY_FAVORITE_LIST = "key_favorite_list"

        @Volatile
        private var instance: FavoriteManager? = null

        fun getInstance(context: Context): FavoriteManager =
            instance ?: synchronized(this) {
                instance ?: FavoriteManager(context.applicationContext).also { instance = it }
            }
    }

    private val prefs =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private fun loadList(): MutableList<RecentItem> {
        val text = prefs.getString(KEY_FAVORITE_LIST, null) ?: return mutableListOf()
        val result = mutableListOf<RecentItem>()
        text.split("\n").forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = line.split("|")
            if (parts.size >= 4) {
                val pkg = parts[0]
                val act = parts[1]
                val label = parts[2]
                val time = parts[3].toLongOrNull() ?: 0L
                result.add(RecentItem(pkg, act, label, time))
            }
        }
        return result
    }

    private fun saveList(list: List<RecentItem>) {
        val text = list.joinToString("\n") {
            "${it.packageName}|${it.activityName}|${it.label}|${it.timeMillis}"
        }
        prefs.edit().putString(KEY_FAVORITE_LIST, text).apply()
    }

    fun getFavorites(): List<RecentItem> = loadList()

    fun isFavorite(pkg: String, activity: String): Boolean =
        loadList().any { it.packageName == pkg && it.activityName == activity }

    fun addFavorite(item: RecentItem) {
        val list = loadList()
        if (list.any { it.packageName == item.packageName && it.activityName == item.activityName }) {
            return
        }
        list.add(0, item)
        saveList(list)
    }

    fun removeFavorite(pkg: String, activity: String) {
        val list = loadList()
        list.removeAll { it.packageName == pkg && it.activityName == activity }
        saveList(list)
    }

    /** NEW — Clear ALL favorites */
    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITE_LIST).apply()
    }
}
