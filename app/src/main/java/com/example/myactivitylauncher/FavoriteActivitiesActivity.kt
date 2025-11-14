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
import com.example.myactivitylauncher.data.FavoriteManager
import com.example.myactivitylauncher.data.RecentItem
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.content.res.Configuration



class FavoriteActivitiesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecentActivitiesAdapter
    private var allItems: List<RecentItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_activities)

        supportActionBar?.title = getString(R.string.title_favorites)

        recycler = findViewById(R.id.recyclerFavorites)
        recycler.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        allItems = FavoriteManager.getInstance(this).getFavorites()

        adapter = RecentActivitiesAdapter(
            pm,
            allItems,
            onClick = { item -> openDetail(item) },
            onLongClick = { item -> confirmDeleteFavorite(item) }
        )

        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        allItems = FavoriteManager.getInstance(this).getFavorites()
        adapter.submitList(allItems)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_favorite, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_activities_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterFavorites(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterFavorites(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
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

    private fun filterFavorites(query: String) {
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

//    private fun confirmDeleteFavorite(item: RecentItem) {
//        AlertDialog.Builder(this)
//            .setTitle(getString(R.string.remove_favorite_title))
//            .setMessage(getString(R.string.remove_favorite_message, item.label))
//            .setPositiveButton(getString(R.string.yes)) { _, _ ->
//                val manager = FavoriteManager.getInstance(this)
//                manager.removeFavorite(item.packageName, item.activityName)
//                allItems = manager.getFavorites()
//                adapter.submitList(allItems)
//                Toast.makeText(this, R.string.removed_from_favorite, Toast.LENGTH_SHORT).show()
//            }
//            .setNegativeButton(getString(R.string.no), null)
//            .show()
//    }

    private fun confirmDeleteFavorite(item: RecentItem) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.remove_favorite_title))
            .setMessage(getString(R.string.remove_favorite_message, item.label))
            .setPositiveButton(getString(R.string.yes)) { d, _ ->
                val manager = FavoriteManager.getInstance(this)
                manager.removeFavorite(item.packageName, item.activityName)

                allItems = manager.getFavorites()
                adapter.submitList(allItems)

                Toast.makeText(this, getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show()
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
