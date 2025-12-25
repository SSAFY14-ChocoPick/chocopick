package com.ssafy.chocopick.data.source.firebase.realtime

import android.util.Log
import com.ssafy.chocopick.data.model.Reward
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "RewardDataSource"

class RewardDataSource(
    private val db: RealtimeDbClient = RealtimeDbClient()
) {
    private fun rewardPath(uid: String) = "${RealtimePaths.REWARDS}/$uid"

    // ✅ 티어 계산 (totalOrders 기준)
    private fun calcTierByTotalOrders(totalOrders: Int): String =
        when {
            totalOrders >= 30 -> "GOLD"
            totalOrders >= 10 -> "SILVER"
            else -> "BRONZE"
        }

    suspend fun getReward(uid: String): Reward? {
        Log.d(TAG, "func getReward / in uid=$uid")
        return try {
            val snap = db.get(rewardPath(uid))
            val value = snap.getValue(Reward::class.java)
            Log.d(TAG, "func getReward / out value=$value")
            value
        } catch (e: CancellationException) {
            Log.w(TAG, "func getReward / cancelled", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "func getReward / ERROR", e)
            null
        }
    }

    suspend fun upsertReward(reward: Reward) {
        db.set(rewardPath(reward.uid), reward)
    }

    /**
     * ✅ 주문 1건에 대해 딱 1번만 Reward 반영 (멱등)
     * - stamps += stampAdd (상품 수량 합계)
     * - totalOrders += 1 (주문 1건)
     * - membershipTier: totalOrders 기준 재계산
     * - /rewards/{uid}/appliedOrders/{orderId} = true
     */
    suspend fun applyRewardForOrderIfNeeded(uid: String, orderId: String, stampAdd: Int): Reward {
        require(uid.isNotBlank()) { "uid is blank" }
        require(orderId.isNotBlank()) { "orderId is blank" }

        val safeStampAdd = stampAdd.coerceAtLeast(0)
        val ref = db.child(rewardPath(uid))

        return suspendCoroutine { cont ->
            ref.runTransaction(object : com.google.firebase.database.Transaction.Handler {

                override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                        : com.google.firebase.database.Transaction.Result {

                    // 현재 Reward 읽기(없으면 생성)
                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)

                    // ✅ 멱등성 체크
                    val alreadyApplied =
                        currentData.child("appliedOrders")
                            .child(orderId)
                            .getValue(Boolean::class.java) == true

                    if (alreadyApplied) {
                        return com.google.firebase.database.Transaction.abort()
                    }

                    // ✅ 반영 후 값 계산
                    val nextTotalOrders = cur.totalOrders + 1
                    val nextTier = calcTierByTotalOrders(nextTotalOrders)
                    val nextStamps = cur.stamps + safeStampAdd

                    // ✅ 안전하게 "필드별로" currentData에 세팅
                    // (appliedOrders 같은 추가 노드가 섞여 있어도 안정적)
                    currentData.child("uid").value = uid
                    currentData.child("stamps").value = nextStamps
                    currentData.child("totalOrders").value = nextTotalOrders
                    currentData.child("membershipTier").value = nextTier
                    currentData.child("updatedAt").value = System.currentTimeMillis()

                    // createdAt은 최초 생성시에만 유지하고 싶으면:
                    if (cur.createdAt == 0L) {
                        currentData.child("createdAt").value = System.currentTimeMillis()
                    } else {
                        currentData.child("createdAt").value = cur.createdAt
                    }

                    // coupon 필드 유지 (Reward에 americanoCoupons 있으니 같이 유지)
                    currentData.child("americanoCoupons").value = cur.americanoCoupons

                    // ✅ 멱등 마킹
                    currentData.child("appliedOrders").child(orderId).value = true

                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (error != null) return cont.resumeWithException(error.toException())

                    // abort(이미 반영됨)이어도 최신 reward 반환
                    val latest = currentData?.getValue(Reward::class.java) ?: Reward(uid = uid)
                    cont.resume(latest)
                }
            })
        }
    }

    // ✅ 기존 구현을 너가 올린 코드 그대로 유지해야 함 (TODO 금지)
    suspend fun issueAmericanoIfPossible(uid: String): Reward {
        val ref = db.child(rewardPath(uid))
        return suspendCoroutine { cont ->
            ref.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                        : com.google.firebase.database.Transaction.Result {

                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)
                    if (cur.stamps < 10) return com.google.firebase.database.Transaction.abort()

                    val updatedStamps = cur.stamps - 10
                    val updatedCoupons = cur.americanoCoupons + 1

                    currentData.child("uid").value = uid
                    currentData.child("stamps").value = updatedStamps
                    currentData.child("americanoCoupons").value = updatedCoupons
                    currentData.child("membershipTier").value = cur.membershipTier
                    currentData.child("totalOrders").value = cur.totalOrders
                    currentData.child("updatedAt").value = System.currentTimeMillis()
                    currentData.child("createdAt").value = cur.createdAt.takeIf { it != 0L } ?: System.currentTimeMillis()

                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (error != null) return cont.resumeWithException(error.toException())
                    if (!committed) return cont.resumeWithException(IllegalStateException("스탬프가 부족합니다."))

                    val latest = currentData?.getValue(Reward::class.java) ?: Reward(uid = uid)
                    cont.resume(latest)
                }
            })
        }
    }

    suspend fun useAmericanoIfPossible(uid: String): Reward {
        val ref = db.child(rewardPath(uid))
        return suspendCoroutine { cont ->
            ref.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                        : com.google.firebase.database.Transaction.Result {

                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)
                    if (cur.americanoCoupons <= 0) return com.google.firebase.database.Transaction.abort()

                    val updatedCoupons = cur.americanoCoupons - 1

                    currentData.child("uid").value = uid
                    currentData.child("americanoCoupons").value = updatedCoupons
                    currentData.child("stamps").value = cur.stamps
                    currentData.child("membershipTier").value = cur.membershipTier
                    currentData.child("totalOrders").value = cur.totalOrders
                    currentData.child("updatedAt").value = System.currentTimeMillis()
                    currentData.child("createdAt").value = cur.createdAt.takeIf { it != 0L } ?: System.currentTimeMillis()

                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (error != null) return cont.resumeWithException(error.toException())
                    if (!committed) return cont.resumeWithException(IllegalStateException("사용 가능한 쿠폰이 없습니다."))

                    val latest = currentData?.getValue(Reward::class.java) ?: Reward(uid = uid)
                    cont.resume(latest)
                }
            })
        }
    }
}
