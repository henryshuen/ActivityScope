package com.example.myactivitylauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryManager private constructor(context: Context) {

    companion object {
        private const val PREF_NAME = "history_prefs"
        private const val KEY_RECENT = "recent_items"
        private const val MAX_HISTORY = 50

        @Volatile
        private var INSTANCE: HistoryManager? = null

        fun getInstance(context: Context): HistoryManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: HistoryManager(context.applicationContext).also { INSTANCE = it }
            }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private fun loadList(): MutableList<RecentItem> {
        val json = prefs.getString(KEY_RECENT, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<RecentItem>>() {}.type
            gson.fromJson<MutableList<RecentItem>>(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveList(list: List<RecentItem>) {
        prefs.edit().putString(KEY_RECENT, gson.toJson(list)).apply()
    }

    /** Add launch record */
    fun recordLaunch(item: RecentItem) {
        val list = loadList()

        list.removeAll { it.packageName == item.packageName && it.activityName == item.activityName }
        list.add(0, item)

        if (list.size > MAX_HISTORY) {
            list.subList(MAX_HISTORY, list.size).clear()
        }

        saveList(list)
    }

    /** Get entire recent list */
    fun getRecentItems(): List<RecentItem> = loadList()

    /** Clear all history */
    fun clearHistory() {
        saveList(emptyList())
    }

    /** Remove single item (existing) */
    fun removeItem(target: RecentItem) {
        val list = loadList()
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.packageName == target.packageName &&
                item.activityName == target.activityName &&
                item.timeMillis == target.timeMillis
            ) {
                iterator.remove()
                break
            }
        }
        saveList(list)
    }

    /** NEW — remove multiple items */
    fun removeItems(targets: List<RecentItem>) {
        val list = loadList()

        val targetSet = targets.toSet()

        val newList = list.filterNot { item ->
            targetSet.any { t ->
                t.packageName == item.packageName &&
                        t.activityName == item.activityName &&
                        t.timeMillis == item.timeMillis
            }
        }

        saveList(newList)
    }
}
