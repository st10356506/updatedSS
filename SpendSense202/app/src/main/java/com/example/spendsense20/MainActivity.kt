package com.example.spendsense20

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendsense20.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottomNav)

        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavView.selectedItemId = R.id.home
        }

        bottomNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.calendar -> replaceFragment(CalendarFragment())
                R.id.goals -> replaceFragment(GoalFragment())
                R.id.add -> replaceFragment(AddFragment())
                R.id.profile -> {
                    // ✅ Pass username and email to ProfileFragment via Bundle
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("username", username)
                            putString("email", email)
                        }
                    }
                    replaceFragment(profileFragment)
                }
            }
            true
        }
    }

    private fun replaceFragment(frag: Fragment) {
        val fragManager = supportFragmentManager
        val fragTransaction = fragManager.beginTransaction()
        fragTransaction.replace(R.id.frameLayout, frag)
        fragTransaction.commit()
    }
}
