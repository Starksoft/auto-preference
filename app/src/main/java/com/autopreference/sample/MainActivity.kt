package com.autopreference.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
//import com.autopreferences.build.AppPreferences
import com.autopreference.sample.screens.DefaultSharedPreferencesFragment
import com.autopreference.sample.screens.UserPreferenceFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val appPreferences = AppPreferences(this)
//
//        val userPreferences = appPreferences.userPreferences
//
//        val userEntity = userPreferences.userEntity
//
//        Log.d(TAG, "userEntity=$userEntity");
    }

    fun onOpenUserSharedPreferencesButtonClicked(v: View) {
        SingleFragmentActivity.start(this, UserPreferenceFragment.TAG)
    }


    fun onOpenDefaultSharedPreferencesButtonClicked(v: View) {
        SingleFragmentActivity.start(this, DefaultSharedPreferencesFragment.TAG)
    }
}