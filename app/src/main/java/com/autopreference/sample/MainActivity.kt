package com.autopreference.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.autopreference.sample.screens.DefaultSharedPreferencesFragment
import com.autopreference.sample.screens.UserPreferenceFragment

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    fun onOpenUserSharedPreferencesButtonClicked(view: View) {
        SingleFragmentActivity.start(this, UserPreferenceFragment.TAG)
    }

    fun onOpenDefaultSharedPreferencesButtonClicked(view: View) {
        SingleFragmentActivity.start(this, DefaultSharedPreferencesFragment.TAG)
    }
}
