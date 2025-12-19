package com.ssafy.chocopick.data.source.firebase.realtime

import com.google.firebase.database.FirebaseDatabase
import com.ssafy.chocopick.data.model.Reward
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RewardDataSource(
    private val rtdb: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private fun rewardRef(uid: String) =
        rtdb.reference.child(RealtimePaths.REWARDS).child(uid)

    suspend fun getReward(uid: String): Reward? {
        val snap = rewardRef(uid).get().await()
        return snap.getValue(Reward::class.java)
    }

    suspend fun upsertReward(reward: Reward) {
        rewardRef(reward.uid).setValue(reward).await()
    }

    /** ✅ stamps 10개 이상이면 stamps-10, americanoCoupons+1 */
    suspend fun issueAmericanoIfPossible(uid: String): Reward {
        return suspendCoroutine { cont ->
            rewardRef(uid).runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)
                    if (cur.stamps < 10) {
                        return com.google.firebase.database.Transaction.abort()
                    }

                    val updated = cur.copy(
                        stamps = cur.stamps - 10,
                        americanoCoupons = cur.americanoCoupons + 1
                    )
                    currentData.value = updated
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resumeWithException(error.toException())
                        return
                    }
                    if (!committed) {
                        cont.resumeWithException(IllegalStateException("스탬프가 부족합니다."))
                        return
                    }
                    val latest = currentData?.getValue(Reward::class.java) ?: Reward(uid = uid)
                    cont.resume(latest)
                }
            })
        }
    }

    suspend fun useAmericanoIfPossible(uid: String): Reward {
        return suspendCoroutine { cont ->
            rewardRef(uid).runTransaction(object : com.google.firebase.database.Transaction.Handler {

                override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                        : com.google.firebase.database.Transaction.Result {

                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)

                    if (cur.americanoCoupons <= 0) {
                        return com.google.firebase.database.Transaction.abort()
                    }

                    val updated = cur.copy(
                        americanoCoupons = cur.americanoCoupons - 1
                    )

                    currentData.value = updated
                    return com.google.firebase.database.Transaction.success(currentData)
                }

                override fun onComplete(
                    error: com.google.firebase.database.DatabaseError?,
                    committed: Boolean,
                    currentData: com.google.firebase.database.DataSnapshot?
                ) {
                    if (error != null) {
                        cont.resumeWithException(error.toException())
                        return
                    }
                    if (!committed) {
                        cont.resumeWithException(IllegalStateException("사용 가능한 쿠폰이 없습니다."))
                        return
                    }
                    val latest = currentData?.getValue(Reward::class.java) ?: Reward(uid = uid)
                    cont.resume(latest)
                }
            })
        }
    }

}
