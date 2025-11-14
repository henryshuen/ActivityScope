package com.example.myactivitylauncher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myactivitylauncher.data.HistoryManager
import com.example.myactivitylauncher.data.RecentItem
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.content.res.Configuration






class RecentActivitiesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecentActivitiesAdapter
    private var allItems: List<RecentItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_activities)

        supportActionBar?.title = getString(R.string.title_recently_launched)

        recycler = findViewById(R.id.recyclerRecent)
        recycler.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        allItems = HistoryManager.getInstance(this).getRecentItems()

        adapter = RecentActivitiesAdapter(
            pm,
            allItems,
            onClick = { item -> openDetail(item) },
            onLongClick = { item -> confirmDeleteOne(item) }
        )

        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        allItems = HistoryManager.getInstance(this).getRecentItems()
        adapter.submitList(allItems)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recent_activities, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_activities_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterRecent(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterRecent(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_recent -> {
                confirmClearAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openDetail(item: RecentItem) {
        val intent = Intent(this, ActivityDetailActivity::class.java).apply {
            putExtra(ActivityDetailActivity.EXTRA_PACKAGE_NAME, item.packageName)
            putExtra(ActivityDetailActivity.EXTRA_ACTIVITY_NAME, item.activityName)
            putExtra(ActivityDetailActivity.EXTRA_LABEL, item.label)
        }
        startActivity(intent)
    }

    private fun filterRecent(query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                item.label.lowercase().contains(q) ||
                        item.packageName.lowercase().contains(q) ||
                        item.activityName.lowercase().contains(q)
            }
        }
        adapter.submitList(filtered)
    }

//    private fun confirmDeleteOne(item: RecentItem) {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.clear_single_title))
//            .setMessage(getString(R.string.clear_single_message, item.label))
//            .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                HistoryManager.getInstance(this).removeItem(item)
//                allItems = HistoryManager.getInstance(this).getRecentItems()
//                adapter.submitList(allItems)
//                Toast.makeText(this, R.string.cleared_one, Toast.LENGTH_SHORT).show()
//            }
//            .setNegativeButton(getString(R.string.no), null)
//            .show()
//    }

    private fun confirmDeleteOne(item: RecentItem) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_single_title))
            .setMessage(getString(R.string.clear_single_message, item.label))
            .setPositiveButton(getString(R.string.yes)) { d, _ ->
                HistoryManager.getInstance(this).removeItem(item)
                allItems = HistoryManager.getInstance(this).getRecentItems()
                adapter.submitList(allItems)
                Toast.makeText(this, getString(R.string.cleared_one), Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNegativeButton(getString(R.string.no), null)
            .create()

        applyDialogTextColor(dialog)
        dialog.show()
    }


//    private fun confirmClearAll() {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.clear_recent))
//            .setMessage(getString(R.string.clear_recent_message))
//            .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                HistoryManager.getInstance(this).clearHistory()
//                allItems = emptyList()
//                adapter.submitList(allItems)
//                Toast.makeText(this, R.string.cleared_all, Toast.LENGTH_SHORT).show()
//            }
//            .setNegativeButton(getString(R.string.no), null)
//            .show()
//    }

    private fun confirmClearAll() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.clear_recent))
            .setMessage(getString(R.string.clear_recent_message))
            .setPositiveButton(getString(R.string.yes)) { d, _ ->
                HistoryManager.getInstance(this).clearHistory()
                allItems = emptyList()
                adapter.submitList(allItems)
                Toast.makeText(this, getString(R.string.cleared_all), Toast.LENGTH_SHORT).show()
                d.dismiss()
            }
            .setNegativeButton(getString(R.string.no), null)
            .create()

        applyDialogTextColor(dialog)
        dialog.show()
    }

    /** Force dialog text and background to match current light / dark mode */
    private fun applyDialogTextColor(dialog: AlertDialog) {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDark = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        val textColor = ContextCompat.getColor(
            this,
            if (isDark) R.color.button_dark_text else R.color.text_primary
        )
        val bgColor = ContextCompat.getColor(
            this,
            if (isDark) R.color.background_dark else R.color.white
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(bgColor))

        val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
        val messageView = dialog.findViewById<TextView>(android.R.id.message)

        titleView?.setTextColor(textColor)
        messageView?.setTextColor(textColor)
    }


}

