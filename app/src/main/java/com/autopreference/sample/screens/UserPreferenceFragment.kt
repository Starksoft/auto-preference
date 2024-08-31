package com.autopreference.sample.screens

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.autopreference.sample.ApplicationInstance
import com.autopreference.sample.R
import ru.starksoft.autopreferences.build.UserPreferences

class UserPreferenceFragment : Fragment() {
    private val userPreferences: UserPreferences =
        ApplicationInstance.instance.appPreferences.getUserPreferences()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_user_preference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEnabled = view.findViewById<CheckBox>(R.id.userEnabled)
        userEnabled.isChecked = userPreferences.isEnabled
        userEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            userPreferences.putEnabled(
                isChecked
            )
        }

        val userName = view.findViewById<EditText>(R.id.userName)
        userName.setText(userPreferences.name)
        userName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                userPreferences.putName(s.toString())
            }
        })

        val userId = view.findViewById<EditText>(R.id.userId)
        userId.setText(userPreferences.id.toString())
        userId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val toString = s.toString()
                if (toString.isEmpty()) {
                    userPreferences.removeId()
                } else {
                    userPreferences.putId(toString.toInt())
                }
            }
        })
    }

    companion object {

        const val TAG: String = "UserPreferenceFragment"

        fun newInstance(): UserPreferenceFragment {
            val args = Bundle()
            val fragment = UserPreferenceFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
