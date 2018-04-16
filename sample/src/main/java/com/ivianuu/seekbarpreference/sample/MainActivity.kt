package com.ivianuu.seekbarpreference.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import com.ivianuu.seekbarpreference.SeekBarPreference
import com.ivianuu.seekbarpreference.ValueTextProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PrefsFragment())
                .commitNowAllowingStateLoss()
        }

    }
}

class PrefsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)

        val pref = findPreference("test2") as SeekBarPreference

        pref.valueTextProvider = object : ValueTextProvider {

            override fun getText(value: Int): String {
                return when {
                    value < 33 -> "Super Small"
                    value < 66 -> "Middle"
                    else -> "Big"
                }
            }

        }
    }
}