package airsign.signage.player.data.downloader

import airsign.signage.player.data.utils.BasePref
import airsign.signage.player.data.utils.ScheduleManager
import airsign.signage.player.domain.model.DeviceModel
import airsign.signage.player.ui.main.MainActivity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "ScheduleWorker"
        const val EXTRA_SCHEDULE_ACTION = "schedule_action"
        const val ACTION_START_SCHEDULE = "start_schedule"
        const val ACTION_END_SCHEDULE = "end_schedule"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val action = inputData.getString(EXTRA_SCHEDULE_ACTION)
            val scheduleId = inputData.getString(EXTRA_SCHEDULE_ID)
            
            Log.d(TAG, "ScheduleWorker triggered with action: $action, scheduleId: $scheduleId")
            
            when (action) {
                ACTION_START_SCHEDULE -> {
                    handleScheduleStart()
                    rescheduleNextOccurrence(scheduleId)
                }
                ACTION_END_SCHEDULE -> {
                    handleScheduleEnd()
                    rescheduleNextOccurrence(scheduleId)
                }
                else -> {
                    Log.w(TAG, "Unknown schedule action: $action")
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in ScheduleWorker", e)
            Result.failure()
        }
    }

    private fun handleScheduleStart() {
        Log.d(TAG, "Starting scheduled playlist - sending broadcast")
        val intent = Intent(MainActivity.ACTION_SCHEDULE_START).apply {
            putExtra("schedule_id", inputData.getString(EXTRA_SCHEDULE_ID))
            setPackage(applicationContext.packageName) // Make it explicit for Android 15+
        }
        applicationContext.sendBroadcast(intent)
        Log.d(TAG, "Broadcast sent: ${intent.action}")
    }

    private fun handleScheduleEnd() {
        Log.d(TAG, "Ending scheduled playlist - sending broadcast")
        val intent = Intent(MainActivity.ACTION_SCHEDULE_END).apply {
            putExtra("schedule_id", inputData.getString(EXTRA_SCHEDULE_ID))
            setPackage(applicationContext.packageName) // Make it explicit for Android 15+
        }
        applicationContext.sendBroadcast(intent)
        Log.d(TAG, "Broadcast sent: ${intent.action}")
    }

    private fun rescheduleNextOccurrence(scheduleId: String?) {
        try {

            if (scheduleId == null) return

            val app = applicationContext as android.app.Application
            val deviceJson = BasePref(app).getString("DEVICE_DETAILS")

            if (deviceJson.isNotEmpty()) {
                val device = Gson().fromJson(deviceJson, DeviceModel::class.java)
                val schedule = device?.schedule

                if (schedule != null && schedule._id == scheduleId) {
                    Log.d(TAG, "Rescheduling next occurrence for schedule: $scheduleId")
                    ScheduleManager.scheduleWorkers(applicationContext, schedule)
                } else {
                    Log.w(TAG, "Schedule not found or ID mismatch for rescheduling")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling next occurrence", e)
        }
    }
}
