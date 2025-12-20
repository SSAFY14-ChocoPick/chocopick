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

    /** stamps 10개 이상이면 stamps-10, americanoCoupons+1 */
    suspend fun issueAmericanoIfPossible(uid: String): Reward {
        val ref = db.child(rewardPath(uid))
        return suspendCoroutine { cont ->
            ref.runTransaction(object : com.google.firebase.database.Transaction.Handler {
                override fun doTransaction(currentData: com.google.firebase.database.MutableData)
                        : com.google.firebase.database.Transaction.Result {

                    val cur = currentData.getValue(Reward::class.java) ?: Reward(uid = uid)
                    if (cur.stamps < 10) return com.google.firebase.database.Transaction.abort()

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

                    val updated = cur.copy(americanoCoupons = cur.americanoCoupons - 1)
                    currentData.value = updated
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