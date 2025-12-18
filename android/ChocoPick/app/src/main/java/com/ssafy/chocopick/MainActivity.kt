package com.ssafy.chocopick

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.databinding.ActivityMainBinding
import com.ssafy.chocopick.ui.home.HomeFragment
import com.ssafy.chocopick.ui.mypage.MyPageFragment
import com.ssafy.chocopick.ui.order.OrderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null){
            binding.bottomNav.selectedItemId = R.id.nav_home
            replaceFragment(HomeFragment())
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener {
            item ->
                when(item.itemId){
                    R.id.nav_home -> replaceFragment(HomeFragment())
                    R.id.nav_order -> replaceFragment(OrderFragment())
                    R.id.nav_mypage -> replaceFragment(MyPageFragment())
                    else -> return@setOnItemSelectedListener false
                }
            true
        }


        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("FCM", "token=$it")
        }

    }

    private fun replaceFragment(fragment : androidx.fragment.app.Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
    }
}