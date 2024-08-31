package com.autopreference.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.autopreference.sample.screens.DefaultSharedPreferencesFragment
import com.autopreference.sample.screens.UserPreferenceFragment

internal class SingleFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragment)

        val screenKey = intent.extras!!.getString(SCREEN_KEY)

        if (screenKey != null) {
            openFragment(screenKey)
        }
    }

    private fun openFragment(screenKey: String) {
        var fragment: Fragment? = null

        when (screenKey) {
            DefaultSharedPreferencesFragment.TAG -> fragment =
                DefaultSharedPreferencesFragment.newInstance()

            UserPreferenceFragment.TAG -> fragment = UserPreferenceFragment.newInstance()
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit()
        }
    }

    companion object {

        const val SCREEN_KEY: String = "screenKey"

        fun start(context: Context, fragment: String?) {
            context.startActivity(Intent(context, SingleFragmentActivity::class.java)
                .apply {
                    putExtra(SCREEN_KEY, fragment)
                })
        }
    }
}
