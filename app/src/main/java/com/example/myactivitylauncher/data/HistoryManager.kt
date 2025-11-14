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

    /** 新增一筆啟動紀錄 */
    fun recordLaunch(item: RecentItem) {
        val list = loadList()

        // 同一個 activity 只留一筆，新的在最前面
        list.removeAll { it.packageName == item.packageName && it.activityName == item.activityName }
        list.add(0, item)

        if (list.size > MAX_HISTORY) {
            list.subList(MAX_HISTORY, list.size).clear()
        }

        saveList(list)
    }

    /** 取得全部 recent list */
    fun getRecentItems(): List<RecentItem> = loadList()

    /** 清空全部紀錄 */
    fun clearHistory() {
        saveList(emptyList())
    }

    /** 刪除單一筆紀錄（給 long press 用） */
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
}
