package com.example.myactivitylauncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myactivitylauncher.data.FavoriteManager
import com.example.myactivitylauncher.data.HistoryManager
import com.example.myactivitylauncher.ui.theme.ThemeHelper

class InstalledPackagesActivity : AppCompatActivity() {

    private lateinit var recyclerPackages: RecyclerView
    private lateinit var adapter: PackagesAdapter
    private lateinit var allApps: List<ApplicationInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        // 先套用主題
        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_packages)

        supportActionBar?.title = "Installed packages"

        recyclerPackages = findViewById(R.id.recyclerPackages)
        recyclerPackages.layoutManager = LinearLayoutManager(this)

        val pm = packageManager

        @Suppress("DEPRECATION")
        allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }

        adapter = PackagesAdapter(pm, allApps) { appInfo ->


            val intent = Intent(this, ActivitiesActivity::class.java)
            intent.putExtra(ActivitiesActivity.EXTRA_PACKAGE_NAME, appInfo.packageName)
            startActivity(intent)
        }

        recyclerPackages.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_installed_packages, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_apps_hint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterApps(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_recent -> {
                startActivity(Intent(this, RecentActivitiesActivity::class.java))
                true
            }
            R.id.action_favorite -> {
                startActivity(Intent(this, FavoriteActivitiesActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            adapter.submitList(allApps)
        } else {
            val pm = packageManager
            val filtered = allApps.filter { info ->
                val label = pm.getApplicationLabel(info).toString()
                label.lowercase().contains(q) ||
                        info.packageName.lowercase().contains(q)
            }
            adapter.submitList(filtered)
        }
    }
}
