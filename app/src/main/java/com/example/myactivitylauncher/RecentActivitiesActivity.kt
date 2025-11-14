package com.example.myactivitylauncher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myactivitylauncher.data.HistoryManager
import com.example.myactivitylauncher.data.RecentItem

// (FIX 2) Imports required for Dialog fixes
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.content.res.Configuration

class RecentActivitiesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecentActivitiesAdapter
    private lateinit var allItems: MutableList<RecentItem>

    private var multiSelectMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_activities)

        supportActionBar?.title = getString(R.string.title_recently_launched)

        recycler = findViewById(R.id.recyclerRecent)
        recycler.layoutManager = LinearLayoutManager(this)

        allItems = HistoryManager.getInstance(this).getRecentItems().toMutableList()

        adapter = RecentActivitiesAdapter(
            packageManager,
            allItems,
            onItemClick = { openDetail(it) },
            onItemLongClick = { enterMultiSelect() },
            onSelectionChanged = { updateSelectionTitle() }
        )

        recycler.adapter = adapter
    }

    // --- (BUG FIX) ---
    // When the Activity returns from the background,
    // onResume() is called, so we reload the list here.
    override fun onResume() {
        super.onResume()
        // 1. Re-fetch the latest list from the database
        allItems = HistoryManager.getInstance(this).getRecentItems().toMutableList()
        // 2. Pass the new list to the adapter for update (this also resets the search filter)
        adapter.submitList(allItems)
    }
    // --- (END OF FIX) ---

    /** Normal click → open detail page */
    private fun openDetail(item: RecentItem) {
        if (!multiSelectMode) {
            val intent = Intent(this, ActivityDetailActivity::class.java)
            intent.putExtra(ActivityDetailActivity.EXTRA_PACKAGE_NAME, item.packageName)
            intent.putExtra(ActivityDetailActivity.EXTRA_ACTIVITY_NAME, item.activityName)
            intent.putExtra(ActivityDetailActivity.EXTRA_LABEL, item.label)
            startActivity(intent)
        } else {
            adapter.toggleSelection(item)
        }
    }

    /** Enter multi-select mode */
    private fun enterMultiSelect() {
        if (!multiSelectMode) {
            multiSelectMode = true
            adapter.enableMultiSelect()
            invalidateOptionsMenu()
            supportActionBar?.title = "Select items"
        }
    }

    /** Title update */
    private fun updateSelectionTitle() {
        val count = adapter.getSelectedCount()
        supportActionBar?.title =
            if (count == 0) "Select items" else "$count selected"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recent_activities, menu)

        val clearButton = menu.findItem(R.id.action_clear_recent)
        clearButton.isVisible = true

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView

        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_clear_recent)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_clear_recent -> {
                if (multiSelectMode && adapter.getSelectedCount() > 0) {
                    confirmDeleteSelected()
                } else {
                    confirmDeleteAll()
                }
                true
            }

            android.R.id.home -> {
                exitMultiSelect()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Delete selected items */
    private fun confirmDeleteSelected() {
        val selected = adapter.getSelectedItems()

        val dialog = AlertDialog.Builder(this) // (FIX 2) Changed to dialog variable
            .setTitle("Delete selected?")
            .setMessage("Are you sure you want to delete ${selected.size} selected items?")
            .setPositiveButton("Delete") { _, _ ->
                HistoryManager.getInstance(this).removeItems(selected)
                allItems.removeAll(selected)
                adapter.submitList(allItems)
                exitMultiSelect()
            }
            .setNegativeButton("Cancel", null)
            .create() // (FIX 2) .create()

        applyDialogTextColor(dialog) // (FIX 2) Call manual fix function
        dialog.show() // (FIX 2) .show()
    }

    /** Delete all items */
    private fun confirmDeleteAll() {
        val dialog = AlertDialog.Builder(this) // (FIX 2) Changed to dialog variable
            .setTitle("Clear all?")
            .setMessage("Are you sure you want to clear all history?")
            .setPositiveButton("Clear") { _, _ ->
                HistoryManager.getInstance(this).clearHistory()
                allItems.clear()
                adapter.submitList(allItems)
                exitMultiSelect()
            }
            .setNegativeButton("Cancel", null)
            .create() // (FIX 2) .create()

        applyDialogTextColor(dialog) // (FIX 2) Call manual fix function
        dialog.show() // (FIX 2) .show()
    }

    private fun exitMultiSelect() {
        multiSelectMode = false
        adapter.disableMultiSelect()
        supportActionBar?.title = getString(R.string.title_recently_launched)
        invalidateOptionsMenu()
    }

    // --- (FIX 2) Function pasted from Favorite file ---
    /** Force dialog text and background to match current light / dark mode */
    private fun applyDialogTextColor(dialog: AlertDialog) {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDark = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        val textColor = ContextCompat.getColor(
            this,
            // (MODIFIED) Ensure Dark Mode text is button_dark_text (white)
            if (isDark) R.color.button_dark_text else R.color.text_primary
        )
        val bgColor = ContextCompat.getColor(
            this,
            // (MODIFIED) Ensure Dark Mode background is background_dark (your #2B2D30)
            if (isDark) R.color.background_dark else R.color.white
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(bgColor))

        val titleView = dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)
        val messageView = dialog.findViewById<TextView>(android.R.id.message)

        titleView?.setTextColor(textColor)
        messageView?.setTextColor(textColor)

        // (BONUS FIX) Also fix button colors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(textColor)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(textColor)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(textColor)
    }
}