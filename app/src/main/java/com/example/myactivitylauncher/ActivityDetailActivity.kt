package com.example.myactivitylauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myactivitylauncher.data.FavoriteManager
import com.example.myactivitylauncher.data.HistoryManager
import com.example.myactivitylauncher.data.RecentItem

class ActivityDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_ACTIVITY_NAME = "extra_activity_name"
        const val EXTRA_LABEL = "extra_label"
    }

    private lateinit var imageIcon: ImageView
    private lateinit var textName: TextView
    private lateinit var textPackage: TextView
    private lateinit var textClass: TextView
    private lateinit var buttonLaunch: Button
    private lateinit var buttonFavorite: Button
    private lateinit var buttonShare: Button

    private lateinit var packageNameStr: String
    private lateinit var activityNameStr: String
    private lateinit var labelStr: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)

        imageIcon = findViewById(R.id.imageIcon)
        textName = findViewById(R.id.textLabel)
        textPackage = findViewById(R.id.textPackage)
        textClass = findViewById(R.id.textClass)
        buttonLaunch = findViewById(R.id.buttonLaunch)
        buttonFavorite = findViewById(R.id.buttonFavorite)
        buttonShare = findViewById(R.id.buttonShare)

        packageNameStr = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        activityNameStr = intent.getStringExtra(EXTRA_ACTIVITY_NAME) ?: ""
        labelStr = intent.getStringExtra(EXTRA_LABEL) ?: activityNameStr

        supportActionBar?.title = "Activity detail"

        if (packageNameStr.isEmpty() || activityNameStr.isEmpty()) {
            Toast.makeText(this, "Activity information incomplete", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textName.text = labelStr
        textPackage.text = packageNameStr
        textClass.text = activityNameStr

        loadIconSafely()

        refreshFavoriteButton()

        buttonLaunch.setOnClickListener {
            launchActivity()
        }

        buttonFavorite.setOnClickListener {
            toggleFavorite()
        }

        buttonShare.setOnClickListener {
            shareInfo()
        }
    }

    private fun loadIconSafely() {
        val pm: PackageManager = packageManager
        val component = ComponentName(packageNameStr, activityNameStr)
        val icon: Drawable? = try {
            pm.getActivityIcon(component)
        } catch (_: Exception) {
            null
        }
        icon?.let { imageIcon.setImageDrawable(it) }
    }

    private fun launchActivity() {
        val component = ComponentName(packageNameStr, activityNameStr)
        val intent = Intent(Intent.ACTION_MAIN).apply {
            this.component = component
            addCategory(Intent.CATEGORY_DEFAULT)
        }

        try {
            startActivity(intent)

            HistoryManager.getInstance(this).recordLaunch(
                RecentItem(
                    packageName = packageNameStr,
                    activityName = activityNameStr,
                    label = labelStr,
                    timeMillis = System.currentTimeMillis()
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "This Activity cannot be started. (${e.javaClass.simpleName})",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toggleFavorite() {
        val manager = FavoriteManager.getInstance(this)
        val currentItem = RecentItem(
            packageName = packageNameStr,
            activityName = activityNameStr,
            label = labelStr,
            timeMillis = System.currentTimeMillis()
        )
        if (manager.isFavorite(packageNameStr, activityNameStr)) {
            manager.removeFavorite(packageNameStr, activityNameStr)
            Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show()
        } else {
            manager.addFavorite(currentItem)
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
        refreshFavoriteButton()
    }

    private fun refreshFavoriteButton() {
        val manager = FavoriteManager.getInstance(this)
        val isFav = manager.isFavorite(packageNameStr, activityNameStr)
        buttonFavorite.text = if (isFav) "Remove from favorite" else "Add to favorite"
    }

    private fun shareInfo() {
        val text = "Package: $packageNameStr\nClass: $activityNameStr"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share Activity Information"))
    }
}
