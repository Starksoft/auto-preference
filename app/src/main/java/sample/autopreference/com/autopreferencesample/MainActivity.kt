package sample.autopreference.com.autopreferencesample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.autopreferences.build.AppPreferences

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appPreferences = AppPreferences(this)

        val userPreferences = appPreferences.userPreferences

        val userEntity = userPreferences.userEntity

        Log.d(TAG, """userEntity=$userEntity""");
    }
}