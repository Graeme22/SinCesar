package com.graemeholliday.sincesar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == "android.intent.action.BOOT_COMPLETED") {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            manager.setRepeating(AlarmManager.RTC, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)
        } else {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val notifications: Int = sharedPrefs.getInt(context.getString(R.string.notify_key), 5)
            val startTime: String? = sharedPrefs.getString(context.getString(R.string.start_key), "09:00")
            val endTime: String? = sharedPrefs.getString(context.getString(R.string.end_key), "21:00")
            //handle times
            val sdf = SimpleDateFormat("HH:mm")
            val current = GregorianCalendar()
            val start = GregorianCalendar().apply {
                set(GregorianCalendar.HOUR_OF_DAY, sdf.parse(startTime).hours)
                set(GregorianCalendar.MINUTE, sdf.parse(startTime).minutes)
            }
            val end = GregorianCalendar().apply {
                set(GregorianCalendar.HOUR_OF_DAY, sdf.parse(endTime).hours)
                set(GregorianCalendar.MINUTE, sdf.parse(endTime).minutes)
            }
            if(start > current || current > end)
                return
            //handle odds
            val diff = (end.timeInMillis - start.timeInMillis).toDouble()
            val probability: Double = notifications.toDouble() / diff * AlarmManager.INTERVAL_FIFTEEN_MINUTES.toDouble()
            if(Math.random() > probability)
                return

            val dao = PrayerDatabase.getDatabase(context).getNotesDao()
            val repository = PrayerRepository(dao)
            CoroutineScope(Dispatchers.Main).launch {
                repository.allNotes.collect { list ->
                    var total: Int = 0
                    for(prayer in list)
                        total += prayer.frequency + 1
                    val choice: Double = Math.random() * total
                    var sum: Int = 0
                    for(prayer in list) {
                        if (prayer.type == R.id.rbExpires) {
                            //handle expiration here
                            val sdf = SimpleDateFormat("dd/MM/yyyy")
                            if(current.time > sdf.parse(prayer.expiration))
                                repository.delete(prayer)
                        }
                        sum += prayer.frequency + 1
                        if(sum >= choice) {
                            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setColor(getColor(context, R.color.purple))
                                .setContentTitle(prayer.title)
                                .setContentText(prayer.description)
                                .setStyle(
                                    NotificationCompat.BigTextStyle().bigText(prayer.description)
                                )
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(true)
                                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                            with(NotificationManagerCompat.from(context)) {
                                // notificationId is a unique int for each notification that you must define
                                notify(prayer.id, builder.build())
                            }
                            if (prayer.type == R.id.rbFixed) {
                                if (prayer.notifications - 1 <= 0)
                                    repository.delete(prayer)
                                else {
                                    val newPrayer = Prayer(
                                        prayer.title,
                                        prayer.description,
                                        prayer.frequency,
                                        prayer.type,
                                        prayer.expiration,
                                        prayer.notifications - 1
                                    )
                                    newPrayer.id = prayer.id
                                    repository.update(newPrayer)
                                }
                            }
                            break
                        }
                    }
                }
            }
        }
    }
}