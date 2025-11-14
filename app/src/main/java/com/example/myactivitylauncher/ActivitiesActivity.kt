package com.example.myactivitylauncher

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ActivitiesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ActivitiesAdapter
    private lateinit var allActivities: List<ActivityInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activities)

        supportActionBar?.title = "Activities"

        recycler = findViewById(R.id.recyclerActivities)
        recycler.layoutManager = LinearLayoutManager(this)

        val pkgName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
        val pm = packageManager

        val packageInfo = try {
            pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES)
        } catch (_: Exception) {
            null
        }

        allActivities = packageInfo?.activities?.toList().orEmpty()
            .sortedBy { it.name }

        adapter = ActivitiesAdapter(pm, allActivities) { info ->
            val label = info.loadLabel(pm)?.toString() ?: info.name
            val intent = Intent(this, ActivityDetailActivity::class.java).apply {
                putExtra(ActivityDetailActivity.EXTRA_PACKAGE_NAME, info.packageName)
                putExtra(ActivityDetailActivity.EXTRA_ACTIVITY_NAME, info.name)
                putExtra(ActivityDetailActivity.EXTRA_LABEL, label)
            }
            startActivity(intent)
        }

        recycler.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activities, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search activities"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterActivities(newText.orEmpty())
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun filterActivities(query: String) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
            adapter.submitList(allActivities)
        } else {
            val filtered = allActivities.filter { info ->
                val label = info.loadLabel(packageManager)?.toString() ?: info.name
                label.lowercase().contains(q) ||
                        info.name.lowercase().contains(q)
            }
            adapter.submitList(filtered)
        }
    }
}
