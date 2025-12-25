package com.ssafy.chocopick

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.ui.common.BeaconInjection
import com.ssafy.chocopick.ui.common.BeaconUiEvent
import com.ssafy.chocopick.ui.common.BeaconViewModel
import com.ssafy.chocopick.ui.common.BeaconViewModelFactory
import com.ssafy.chocopick.data.source.firebase.messaging.NotificationHelper
import com.ssafy.chocopick.databinding.ActivityMainBinding
import com.ssafy.chocopick.ui.common.CurrentUserViewModel
import com.ssafy.chocopick.ui.common.CurrentUserViewModelFactory
import com.ssafy.chocopick.ui.common.NfcViewModel
import com.ssafy.chocopick.ui.home.HomeFragment
import com.ssafy.chocopick.ui.mypage.MyPageFragment
import com.ssafy.chocopick.ui.order.ProductListFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private val currentUserVm: CurrentUserViewModel by viewModels {
        CurrentUserViewModelFactory()
    }

    private val beaconVM : BeaconViewModel by viewModels {
        BeaconViewModelFactory(BeaconInjection.provideRepo(application))
    }
    private val reqBlePerms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = hasBlePermissionResult(result)
        if (granted) beaconVM.startScanning()
        // 거절이면 조용히 스캔 안 함 (원하면 Toast/다이얼로그 추가)
    }

    // ✅ 알림 권한 요청 (Android 13+)
    private val reqNotiPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted -> 필요하면 로그/안내 */ }

    private val nfcViewModel : NfcViewModel by viewModels()
    private var entryDialog: AlertDialog? = null

    private val nfcAdapter: NfcAdapter? by lazy { NfcAdapter.getDefaultAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("NFC_ROUTE", "MainActivity onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createFcmChannel()
        observeNfcWaiting()

        // ✅ 앱 시작 시 딱 1번만 내 정보 로드
        currentUserVm.loadMe()

        collectBeaconEvents()
        handleNfcIntent(intent)

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

    private fun observeNfcWaiting() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                nfcViewModel.waiting.collect { waiting ->
                    if (waiting) enableReaderMode()
                    else disableReaderMode()
                }
            }
        }
    }
    private fun enableReaderMode() {
        val adapter = nfcAdapter ?: return

        adapter.enableReaderMode(
            this,
            { tag ->
                Log.d("NFC", "ReaderMode tag=$tag")
                // ✅ 여기서 주문 트리거
                runOnUiThread { nfcViewModel.onTagDetected() }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, // ✅ 시스템 NDEF 처리(스토어 팝업) 스킵
            null
        )
    }

    private fun disableReaderMode() {
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("NFC_ROUTE", "MainActivity onNewIntent action=${intent.action}")

        setIntent(intent)
        handleNfcIntent(intent)   // ✅ 공통 처리
    }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action ?: return

        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            Log.d("NFC", "handleNfcIntent: detected action=$action")
            nfcViewModel.onTagDetected()

            // (선택) 태그 정보도 같이 쓰고 싶으면:
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            Log.d("NFC", "tag=$tag")
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
    @SuppressLint("MissingPermission")
    private fun collectBeaconEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                beaconVM.uiEvent.collect { ev ->
                    Log.d("Beacon", "ENTER event -> show notification")

                    if (ev is BeaconUiEvent.ShowEntryNotification) {
                        // ✅ 여기서 권한 체크를 확실히 해줘서 빨간줄 해결 + 런타임 안전
                        Log.d("Beacon", "ENTER event received -> try show notification")

                        if (canPostNotifications()) {
                            NotificationHelper.showEntry(this@MainActivity)
                            showEntryDialog()
                        } else {

                            // 권한 없으면 알림 대신 로그만 (원하면 Toast)
                            Log.d("Beacon", "POST_NOTIFICATIONS not granted; skip notification")
                        }
                    }
                }
            }
        }
    }

    private fun ensureNotiPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (!canPostNotifications()) {
                reqNotiPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestBlePermsIfNeeded() {
        val perms = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= 31) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) perms += Manifest.permission.BLUETOOTH_SCAN

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) perms += Manifest.permission.BLUETOOTH_CONNECT
            if(ContextCompat.checkSelfPermission(
                this,Manifest.permission.ACCESS_FINE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED
            ) perms+=Manifest.permission.ACCESS_FINE_LOCATION
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) perms += Manifest.permission.ACCESS_FINE_LOCATION
        }

        // 이미 다 있으면 바로 스캔 시작
        if (perms.isEmpty()) {
            beaconVM.startScanning()
        } else {
            reqBlePerms.launch(perms.toTypedArray())
        }
    }

    private fun hasBlePermissionResult(result: Map<String, Boolean>): Boolean {
        return if (Build.VERSION.SDK_INT >= 31) {
            (result[Manifest.permission.BLUETOOTH_SCAN] == true) &&
                    (result[Manifest.permission.BLUETOOTH_CONNECT] == true)
        } else {
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showEntryDialog(
        title: String = "이용 알림",
        message: String = "1. 스마트 스토어 방문을\n환영합니다. 즐거운 시간 보내세요.\n\n" +
                "2. 매장 내 이용시\n테이블의 NFC를 태그하면\n해당 테이블로 물품을\n가져다드립니다."
    ) {
        // ✅ 중복 방지: 이미 떠있으면 또 띄우지 않기
        if (entryDialog?.isShowing == true) return

        entryDialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("확인") { d, _ -> d.dismiss() }
            .create()

        entryDialog?.show()
    }

    override fun onStart() {
        super.onStart()

        ensureNotiPermissionIfNeeded()   // Android 13+ 알림 권한
        requestBlePermsIfNeeded()        // BLE 스캔 권한 확인 후 startScanning 호출
    }

    override fun onStop() {
        super.onStop()
        entryDialog?.dismiss()
        entryDialog = null
        beaconVM.stopScanning()
    }

}


