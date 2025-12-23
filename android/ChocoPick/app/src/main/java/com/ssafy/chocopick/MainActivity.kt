package com.ssafy.chocopick

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.chocopick.data.source.beacon.BeaconInjection
import com.ssafy.chocopick.data.source.beacon.BeaconUiEvent
import com.ssafy.chocopick.data.source.beacon.BeaconViewModel
import com.ssafy.chocopick.data.source.beacon.BeaconViewModelFactory
import com.ssafy.chocopick.data.source.firebase.messaging.NotificationHelper
import com.ssafy.chocopick.databinding.ActivityMainBinding
import com.ssafy.chocopick.ui.common.CurrentUserViewModel
import com.ssafy.chocopick.ui.common.CurrentUserViewModelFactory
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


    private var entryDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createFcmChannel()

        // ✅ 앱 시작 시 딱 1번만 내 정보 로드
        currentUserVm.loadMe()

        collectBeaconEvents()

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



/*
✅ 비콘 테스트 당일 체크리스트 (실전용)
0️⃣ 준비물 (집 나오기 전)

 실기기 안드로이드폰 (에뮬레이터 ❌)

 비콘 (전원 ON / 배터리 충분)

 nRF Connect 앱 설치

 ChocoPick 앱 최신 빌드 설치

1️⃣ 비콘 자체 확인 (앱 실행 ❌, 먼저 이거부터)

👉 이 단계에서 문제 나면 앱 테스트 의미 없음

🔹 nRF Connect로 확인

nRF Connect 실행

Scan 시작

비콘 발견

비콘 클릭

✅ 여기서 반드시 확인할 것

 iBeacon으로 표시되는지

 Manufacturer Data에 Apple (0x004C) + 02 15

 UUID / Major / Minor 값 기록 (캡처 or 메모)

❌ 여기서 안 보이면
→ 비콘 전원 / 광고 설정 / 거리 문제
→ 앱 테스트 중단

2️⃣ ChocoPick 코드에 “내 비콘” 값 고정

👉 집이나 테스트 장소에서 딱 1번만 하면 됨

 AltBeacon Region에 UUID/Major/Minor 반영

 Region("target-beacon", UUID, Major, Minor) 형태 확인

 빌드 & 설치

3️⃣ 테스트 환경 세팅 (앱 실행 직전)

 휴대폰 Bluetooth ON

 휴대폰 위치(Location) ON

 앱 실행 시 권한 전부 허용

Android 12+: 근처 기기(BLE)

Android 13+: 알림

4️⃣ 로그로 “스캔이 도는지” 확인 (제일 중요)

👉 알림보다 로그가 먼저

Logcat 필터
Beacon

반드시 보여야 하는 로그

 ScanJob Lifecycle START

 ranged count=1 이상

 nearest distance=...

❌ ranged count=0만 나오면
→ 비콘이 잡히지 않는 상태
→ 다시 1️⃣로 돌아가기

5️⃣ 거리 테스트 방법 (실패 안 하는 방식)

RSSI는 튀니까 이렇게 해야 성공률 높아.

비콘에서 3~5m 떨어진 상태에서 시작

앱 실행 (포그라운드)

천천히 비콘 쪽으로 이동

Logcat에서 distance 값 확인

distance가 1.0 이하로 연속 찍히는지 확인

6️⃣ ENTER 이벤트 확인

👉 이 로그가 핵심

 ENTER detected distance=...

 emit ShowEntryNotification

 MainActivity received event

❌ 여기까지 로그는 나오는데 알림 안 뜨면
→ 다음 단계로

7️⃣ 알림 최종 확인

 알림 권한 허용돼 있는지

 앱 알림 설정에서 “매장 입장 알림” 채널 ON

 방해 금지 / 무음 모드 확인

👉 여기까지 통과하면 100% 성공

🧪 빠른 테스트용 임시 설정 (강력 추천)

처음엔 조건을 느슨하게 해서 확인해.

 입장 거리: 2.0m

 연속 횟수: 1회

 쿨다운: 5초

✔️ 알림 뜨는 거 확인
✔️ 그 다음 요구사항대로 다시 조이기

🔥 테스트 실패 시 원인 분기표
증상	원인
nRF Connect에서도 안 보임	비콘 문제
nRF는 보이는데 앱은 0개	UUID/Major/Minor 불일치
distance 로그는 있음	조건/쿨다운 문제
이벤트 로그 있음	알림 권한/채널 문제
✅ 최종 요약 (이것만 기억해도 됨)

nRF Connect로 비콘 먼저 확인

UUID/Major/Minor 고정

로그 → 이벤트 → 알림 순서로 확인

처음엔 조건 느슨하게

이따 테스트하다가

로그 한 장 캡처

또는 “어디 단계에서 막혔는지”

만 보내주면,
그 지점 기준으로 바로 해결책만 콕 집어서 도와줄게.


 */