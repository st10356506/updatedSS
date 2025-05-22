package com.example.spendsense20

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.spendsense20.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavView : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        bottomNavView = findViewById(R.id.bottomNav)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            bottomNavView.selectedItemId = R.id.home
        }


        bottomNavView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.calendar -> replaceFragment(CalendarFragment())
                R.id.goals -> replaceFragment(GoalFragment())
                R.id.add -> replaceFragment(AddFragment())
                R.id.profile -> {
                    val profileFragment = ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString("username", "userUsername")  // Replace with actual username
                            putString("email", "userEmail") // Replace with actual email
                        }
                    }
                    replaceFragment(profileFragment)
                }
            }
            true
        }

    }

    fun replaceFragment(frag : Fragment){
        var fragManager = supportFragmentManager
        var fragTransaction = fragManager.beginTransaction()
        fragTransaction.replace(R.id.frameLayout,frag)
        fragTransaction.commit()
    }

}