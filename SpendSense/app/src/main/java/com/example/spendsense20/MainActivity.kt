package com.example.spendsense20

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendsense20.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
//initialize views
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Android Developers (2025). Progress indicators. Available at: https://developer.android.com/develop/ui/compose/components/progress
        setContentView(R.layout.activity_main) // Developers, A. (n.d.). Build a UI with Layout Editor. Available at: https://developer.android.com/studio/write/layout-editor

        bottomNavView = findViewById(R.id.bottomNav) // Developers, A. (n.d.). Spinners. Available at: https://developer.android.com/develop/ui/views/components/spinner

        val username = intent.getStringExtra("username") ?: "" // Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments
        val email = intent.getStringExtra("email") ?: "" // Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment()) // Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments
            bottomNavView.selectedItemId = R.id.home
        }
        //setup nav bar
// Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments
        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.calendar -> replaceFragment(CalendarFragment())
                R.id.goals -> replaceFragment(GoalFragment())
                R.id.add -> replaceFragment(AddFragment())
                R.id.profile -> {
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("username", username) // Aamjit Yanglem (2022). How do I add a functional edit icon for a profile picture in Android Studio? Available at: https://stackoverflow.com/questions/71860869/how-do-i-add-a-functional-edit-icon-for-a-profile-picture-in-android-studio
                            putString("email", email) // Aamjit Yanglem (2022). How do I add a functional edit icon for a profile picture in Android Studio? Available at: https://stackoverflow.com/questions/71860869/how-do-i-add-a-functional-edit-icon-for-a-profile-picture-in-android-studio
                        }
                    }
                    replaceFragment(profileFragment) // Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments
                }
            }
            true
        }
    }
//replace the fragments
    private fun replaceFragment(frag: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, frag)
            .commit() // Android Developers (n.d.). Fragments. Available at: https://developer.android.com/guide/fragments
    }
}
