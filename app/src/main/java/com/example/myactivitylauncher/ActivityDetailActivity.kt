package com.example.myactivitylauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Build
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
    private lateinit var textLabel: TextView
    private lateinit var textPackage: TextView
    private lateinit var textClass: TextView
    private lateinit var buttonLaunch: Button
    private lateinit var buttonFavorite: Button
    private lateinit var buttonShortcut: Button

    private var packageNameStr: String = ""
    private var activityNameStr: String = ""
    private var labelStr: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_detail)

        imageIcon = findViewById(R.id.imageIcon)
        textLabel = findViewById(R.id.textLabel)
        textPackage = findViewById(R.id.textPackage)
        textClass = findViewById(R.id.textClass)
        buttonLaunch = findViewById(R.id.buttonLaunch)
        buttonFavorite = findViewById(R.id.buttonFavorite)
        buttonShortcut = findViewById(R.id.buttonShortcut)

        val intent = intent
        packageNameStr = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
        activityNameStr = intent.getStringExtra(EXTRA_ACTIVITY_NAME) ?: ""
        labelStr = intent.getStringExtra(EXTRA_LABEL) ?: ""

        if (packageNameStr.isEmpty() || activityNameStr.isEmpty()) {
            Toast.makeText(this, "Invalid activity info", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        textLabel.text = if (labelStr.isNotEmpty()) labelStr else activityNameStr
        textPackage.text = packageNameStr
        textClass.text = activityNameStr

        loadIconSafely()
        refreshFavoriteButton()

        buttonLaunch.setOnClickListener { launchActivity() }
        buttonFavorite.setOnClickListener { toggleFavorite() }
        buttonShortcut.setOnClickListener { addHomeScreenShortcut() }
    }

    /** Load the target Activity's icon. Fallback to app icon if failed. */
    private fun loadIconSafely() {
        val pm: PackageManager = packageManager
        try {
            val component = ComponentName(packageNameStr, activityNameStr)
            val activityIcon = pm.getActivityIcon(component)
            imageIcon.setImageDrawable(activityIcon)
        } catch (e: Exception) {
            try {
                val appIcon = pm.getApplicationIcon(packageNameStr)
                imageIcon.setImageDrawable(appIcon)
            } catch (e2: Exception) {
                imageIcon.setImageResource(R.mipmap.ic_launcher)
            }
        }
    }

    /** Convert Drawable to Bitmap for shortcut icon usage. */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /** Launch the selected Activity and record history. */
    private fun launchActivity() {
        try {
            val component = ComponentName(packageNameStr, activityNameStr)
            val intent = Intent(Intent.ACTION_MAIN).apply {
                this.component = component
                addCategory(Intent.CATEGORY_LAUNCHER)
                addCategory(Intent.CATEGORY_DEFAULT)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            startActivity(intent)

            // Record launch history
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
            Toast.makeText(this, "Unable to launch activity", Toast.LENGTH_SHORT).show()
        }
    }

    /** Toggle favorite state. */
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
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            manager.addFavorite(currentItem)
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        }

        refreshFavoriteButton()
    }

    /** Update button label depending on favorite state. */
    private fun refreshFavoriteButton() {
        val manager = FavoriteManager.getInstance(this)
        val isFav = manager.isFavorite(packageNameStr, activityNameStr)
        buttonFavorite.text = if (isFav) {
            // Use existing string: "Remove favorite"
            getString(R.string.remove_favorite_title)
        } else {
            // Use existing menu label: "Favorites" (acts as "Add to favorites" button)
            getString(R.string.menu_favorite)
        }
    }

    /** Add home screen shortcut using the target Activity's icon. */
    private fun addHomeScreenShortcut() {
        val shortcutLabel = if (labelStr.isNotEmpty()) labelStr else activityNameStr

        // Try using activity icon; fallback to application icon if needed
        val pm = packageManager
        val component = ComponentName(packageNameStr, activityNameStr)
        val activityIconDrawable: Drawable? = try {
            pm.getActivityIcon(component)
        } catch (e: Exception) {
            try {
                pm.getApplicationIcon(packageNameStr)
            } catch (e2: Exception) {
                null
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)
            if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported) {
                // Launcher does not support pinned shortcuts at all
                Toast.makeText(
                    this,
                    "Home screen shortcuts are not supported on this launcher",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val shortcutIntent = Intent(Intent.ACTION_MAIN).apply {
                this.component = component
                addCategory(Intent.CATEGORY_DEFAULT)
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val icon = activityIconDrawable?.let { drawableToBitmap(it) }?.let {
                Icon.createWithBitmap(it)
            } ?: Icon.createWithResource(this, R.mipmap.ic_launcher)

            val shortcutId = "activity_${packageNameStr}_${activityNameStr}"

            val shortcut = ShortcutInfo.Builder(this, shortcutId)
                .setShortLabel(shortcutLabel)
                .setLongLabel(shortcutLabel)
                .setIcon(icon)
                .setIntent(shortcutIntent)
                .build()

            // No callback: we let the system UI handle success / failure feedback.
            shortcutManager.requestPinShortcut(shortcut, null)
        } else {
            // Legacy method for older launchers (no reliable success/failure callback)
            val shortcutIntent = Intent(Intent.ACTION_MAIN).apply {
                this.component = component
                addCategory(Intent.CATEGORY_DEFAULT)
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val addIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutLabel)

                val bitmap = activityIconDrawable?.let { drawableToBitmap(it) }
                if (bitmap != null) {
                    putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                } else {
                    putExtra(
                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(
                            this@ActivityDetailActivity,
                            R.mipmap.ic_launcher
                        )
                    )
                }
            }

            sendBroadcast(addIntent)
            // No toast here either, to avoid immediate noisy feedback.
        }
    }
}
