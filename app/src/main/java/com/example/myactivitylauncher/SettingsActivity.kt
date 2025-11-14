package com.example.myactivitylauncher


import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myactivitylauncher.ui.theme.ThemeHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var radioSystem: RadioButton
    private lateinit var radioLight: RadioButton
    private lateinit var radioDark: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {

        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.menu_settings)

        radioGroupTheme = findViewById(R.id.radioGroupTheme)
        radioSystem = findViewById(R.id.radioSystem)
        radioLight = findViewById(R.id.radioLight)
        radioDark = findViewById(R.id.radioDark)


        when (ThemeHelper.getSavedTheme(this)) {
            ThemeHelper.THEME_SYSTEM -> radioSystem.isChecked = true
            ThemeHelper.THEME_LIGHT -> radioLight.isChecked = true
            ThemeHelper.THEME_DARK -> radioDark.isChecked = true
        }

        radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val targetTheme = when (checkedId) {
                R.id.radioLight -> ThemeHelper.THEME_LIGHT
                R.id.radioDark -> ThemeHelper.THEME_DARK
                else -> ThemeHelper.THEME_SYSTEM
            }
            ThemeHelper.saveTheme(this, targetTheme)
            ThemeHelper.applyTheme(this)
            recreate()
        }


        val textAuthor: TextView = findViewById(R.id.textAboutAuthor)
        val textBasedOn: TextView = findViewById(R.id.textAboutBasedOn)
        val textSource: TextView = findViewById(R.id.textAboutSource)

        textAuthor.text = getString(R.string.settings_about_author)
        textBasedOn.text = getString(R.string.settings_about_based_on)
        textSource.text = getString(R.string.settings_about_source)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}