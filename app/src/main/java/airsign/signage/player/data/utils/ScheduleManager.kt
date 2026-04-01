package airsign.signage.player.data.utils

import airsign.signage.player.data.downloader.ScheduleWorker
import airsign.signage.player.domain.model.ScheduleInfo
import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat.forPattern
import java.util.Locale
import java.util.concurrent.TimeUnit

object ScheduleManager {
    private const val TAG = "ScheduleManager"


    fun hasSchedule(schedule: ScheduleInfo?): Boolean = schedule != null

    fun isScheduleActive(schedule: ScheduleInfo?): Boolean {
        if (schedule == null || schedule.toString().isEmpty()) return true
        
        try {
            val now = DateTime.now()
            
            val startDate = DateTime.parse(schedule.startDate)
            val endDate = DateTime.parse(schedule.endDate)
            
            Log.d(TAG, "Checking date range - Now: $now, Start: $startDate, End: $endDate")
            
            if (now.isBefore(startDate) || now.isAfter(endDate)) {
                Log.d(TAG, "Current time is outside schedule date range")
                return false
            }

            val startTime = forPattern("HH:mm").parseLocalTime(schedule.startTime)
            val endTime = forPattern("HH:mm").parseLocalTime(schedule.endTime)

            val currentTime = now.toLocalTime()
            
            Log.d(TAG, "Checking time range - Current: $currentTime, Start: $startTime, End: $endTime")
            
            if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                Log.d(TAG, "Current time is outside schedule time range")
                return false
            }

            if (schedule.daysOfWeek.isNotEmpty()) {
                val currentDay = now.dayOfWeek().getAsText(Locale.ENGLISH).uppercase(Locale.ROOT)
                val scheduleDays = schedule.daysOfWeek.map { it.uppercase(Locale.ROOT) }

                if (currentDay !in scheduleDays) return false
            }
            
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking schedule status", e)
            return true
        }
    }

    fun scheduleWorkers(context: Context, schedule: ScheduleInfo) {
        if (!hasSchedule(schedule)) {
            Log.d(TAG, "No valid schedule found, not scheduling workers")
            return
        }

        try {
            val workManager = WorkManager.getInstance(context)
            cancelScheduleWorkers(context, schedule._id)

            scheduleStartWorker(workManager, schedule)
            scheduleEndWorker(workManager, schedule)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling workers", e)
        }
    }

    private fun scheduleStartWorker(workManager: WorkManager, schedule: ScheduleInfo) {
        try {

            val now = DateTime.now()
            val endDate = DateTime.parse(schedule.endDate)

            val startTime = forPattern("HH:mm").parseLocalTime(schedule.startTime)

            if (now.isAfter(endDate)) {
                Log.d(TAG, "Schedule end date has passed, not scheduling start worker")
                return
            }

            var scheduleStartDateTime = now.withTime(startTime)

            if (scheduleStartDateTime.isBefore(now) || scheduleStartDateTime.isEqual(now)) {
                scheduleStartDateTime = scheduleStartDateTime.plusDays(1)
            }

            if (scheduleStartDateTime.isAfter(endDate)) {
                Log.d(TAG, "Next start time would be after end date, not scheduling")
                return
            }

            if (schedule.daysOfWeek.isNotEmpty()) {
                val scheduledDay = scheduleStartDateTime.dayOfWeek().getAsText(Locale.ENGLISH)
                    .uppercase(Locale.ROOT)
                val scheduleDays = schedule.daysOfWeek.map { it.uppercase(Locale.ROOT) }

                if (scheduledDay !in scheduleDays) {
                    var attempts = 0

                    while (attempts < 7) {
                        scheduleStartDateTime = scheduleStartDateTime.plusDays(1)
                        val dayName = scheduleStartDateTime.dayOfWeek().getAsText(Locale.ENGLISH)
                            .uppercase(Locale.ROOT)
                        if (dayName in scheduleDays && scheduleStartDateTime.isBefore(endDate)) {
                            break
                        }
                        attempts++
                    }

                    if (attempts >= 7 || scheduleStartDateTime.isAfter(endDate)) {
                        return
                    }
                }
            }

            val delayMillis = scheduleStartDateTime.millis - now.millis
            val delaySeconds = delayMillis / 1000

            Log.d(
                TAG,
                "Scheduling start worker - Delay: ${delaySeconds}s, Target time: $scheduleStartDateTime"
            )

            if (delayMillis > 0) {
                val data = Data.Builder()
                    .putString(ScheduleWorker.EXTRA_SCHEDULE_ACTION, ScheduleWorker.ACTION_START_SCHEDULE)
                    .putString(ScheduleWorker.EXTRA_SCHEDULE_ID, schedule._id)
                    .build()

                val startWorkRequest = OneTimeWorkRequestBuilder<ScheduleWorker>()
                    .setInputData(data).setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag("schedule_start_${schedule._id}")
                    .build()

                workManager.enqueue(startWorkRequest)
                Log.d(
                    TAG,
                    "Successfully scheduled start worker for $scheduleStartDateTime (in ${delaySeconds}s)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling start worker", e)
        }
    }

    private fun scheduleEndWorker(workManager: WorkManager, schedule: ScheduleInfo) {
        try {
            val now = DateTime.now()
            val startDate = DateTime.parse(schedule.startDate)
            val endDate = DateTime.parse(schedule.endDate)
            val endTime = forPattern("HH:mm").parseLocalTime(schedule.endTime)

            if (now.isAfter(endDate)) {
                Log.d(TAG, "Schedule end date has passed, not scheduling end worker")
                return
            }

            var scheduleEndDateTime = now.withTime(endTime)

            if (scheduleEndDateTime.isBefore(now) || scheduleEndDateTime.isEqual(now)) {
                scheduleEndDateTime = scheduleEndDateTime.plusDays(1)
                Log.d(TAG, "Today's end time passed, scheduling for tomorrow: $scheduleEndDateTime")
            }

            if (scheduleEndDateTime.isAfter(endDate)) {
                Log.d(TAG, "Next end time would be after end date, not scheduling")
                return
            }

            if (schedule.daysOfWeek.isNotEmpty()) {
                val scheduledDay =
                    scheduleEndDateTime.dayOfWeek().getAsText(Locale.ENGLISH).uppercase(Locale.ROOT)
                val scheduleDays = schedule.daysOfWeek.map { it.uppercase(Locale.ROOT) }

                if (scheduledDay !in scheduleDays) {
                    Log.d(
                        TAG,
                        "Next end day ($scheduledDay) is not in schedule days, finding next valid day"
                    )
                    var attempts = 0
                    while (attempts < 7) {
                        scheduleEndDateTime = scheduleEndDateTime.plusDays(1)
                        val dayName = scheduleEndDateTime.dayOfWeek().getAsText(Locale.ENGLISH)
                            .uppercase(Locale.ROOT)
                        if (dayName in scheduleDays && scheduleEndDateTime.isBefore(endDate)) {
                            break
                        }
                        attempts++
                    }

                    if (attempts >= 7 || scheduleEndDateTime.isAfter(endDate)) {
                        Log.d(TAG, "Could not find valid end day within schedule")
                        return
                    }
                }
            }

            val delayMillis = scheduleEndDateTime.millis - now.millis
            val delaySeconds = delayMillis / 1000

            Log.d(
                TAG,
                "Scheduling end worker - Delay: ${delaySeconds}s, Target time: $scheduleEndDateTime"
            )

            if (delayMillis > 0) {
                val data = Data.Builder()
                    .putString(ScheduleWorker.EXTRA_SCHEDULE_ACTION, ScheduleWorker.ACTION_END_SCHEDULE)
                    .putString(ScheduleWorker.EXTRA_SCHEDULE_ID, schedule._id)
                    .build()

                val endWorkRequest = OneTimeWorkRequestBuilder<ScheduleWorker>()
                    .setInputData(data).setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag("schedule_end_${schedule._id}")
                    .build()

                workManager.enqueue(endWorkRequest)
                Log.d(
                    TAG,
                    "Successfully scheduled end worker for $scheduleEndDateTime (in ${delaySeconds}s)"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling end worker", e)
        }
    }

    private fun cancelScheduleWorkers(context: Context, scheduleId: String) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWorkByTag("schedule_start_$scheduleId")
            workManager.cancelAllWorkByTag("schedule_end_$scheduleId")
            Log.d(TAG, "Cancelled workers for schedule: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling schedule workers", e)
        }
    }
}
