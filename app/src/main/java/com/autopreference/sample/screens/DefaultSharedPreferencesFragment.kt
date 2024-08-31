package com.autopreference.sample.screens

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.autopreference.sample.R

internal class DefaultSharedPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        setPreferencesFromResource(R.xml.default_preferences, null)
    }

    companion object {

        const val TAG: String = "DefaultSharedPreference"

        fun newInstance(): DefaultSharedPreferencesFragment {
            val args = Bundle()
            val fragment = DefaultSharedPreferencesFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
