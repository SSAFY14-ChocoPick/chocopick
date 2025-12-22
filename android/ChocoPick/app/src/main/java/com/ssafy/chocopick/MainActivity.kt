package com.ssafy.chocopick

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.databinding.ActivityMainBinding
import com.ssafy.chocopick.ui.common.CurrentUserViewModel
import com.ssafy.chocopick.ui.common.CurrentUserViewModelFactory
import com.ssafy.chocopick.ui.home.HomeFragment
import com.ssafy.chocopick.ui.mypage.MyPageFragment
import com.ssafy.chocopick.ui.order.ProductListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private val currentUserVm: CurrentUserViewModel by viewModels {
        CurrentUserViewModelFactory()
    }
    private val requestNotiPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // granted false면 알림 안 뜸
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ensureNotiPermission()
        createFcmChannel()

        // ✅ 앱 시작 시 딱 1번만 내 정보 로드
        currentUserVm.loadMe()

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
                    R.id.nav_order -> replaceFragment(ProductListFragment())
                    R.id.nav_mypage -> replaceFragment(MyPageFragment())
                    else -> return@setOnItemSelectedListener false
                }
            true
        }


        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("FCM", "token=$it")
        }

    }

    fun createFcmChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "chocopick_fcm_v2"
            val channel = NotificationChannel(
                channelId,
                "ChocoPick 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "주문/픽업 알림"
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun replaceFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()
    }
    private fun ensureNotiPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                requestNotiPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}