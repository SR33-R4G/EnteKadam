package in.entekadam.app.util

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("debt_due", "Payment reminders", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = NotificationCompat.Builder(context, "debt_due")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(intent.getStringExtra("title") ?: "Ente Kadam")
            .setContentText(intent.getStringExtra("message") ?: "Payment reminder")
            .setAutoCancel(true).build()
        manager.notify(intent.getLongExtra("id", 0).toInt(), notification)
    }
}

fun scheduleReminder(context: Context, id: Long, at: Long, personName: String, amount: String) {
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("id", id); putExtra("title", "Payment reminder"); putExtra("message", "$personName · $amount")
    }
    val pending = PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
}
