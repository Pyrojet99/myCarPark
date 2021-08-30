package giacomo.cignoni.testandroid.mycarpark;

import static giacomo.cignoni.testandroid.mycarpark.AlarmUtility.FIVE_MIN_IN_MILLIS;
import static giacomo.cignoni.testandroid.mycarpark.AlarmUtility.NOTIFICATION_CHANNEL_ID;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.List;

public class RestoreAlarmsService extends IntentService {
    private static int SERVICE_ID = 1234567;
    private static int NOTIFICATION_ID = 1111;

    public RestoreAlarmsService() {
        super("RestoreAlarmsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //build and show notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(getString(R.string.boot_notification_title))
                        .setContentText(getString(R.string.boot_notification_text))
                        .setSmallIcon(R.drawable.ic_notification_car_24)
                        .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        Notification n = notificationBuilder.build();
        notificationManager.notify(NOTIFICATION_ID, n);

        startForeground(SERVICE_ID, n);

        AppDatabase db = AppDatabase.getDatabase(this);
        //get DAOs
        ParkDao parkDao = db.parkDao();
        CarDao carDao = db.carDao();
        List<Park> parksWithAlarm = parkDao.getParksWithAlarm();

        //get current time in millis
        long currentTime = Calendar.getInstance().getTimeInMillis();

        for(Park p : parksWithAlarm) {
            if(p.getAlarmTime() > currentTime) {
                //sets only future alarms
                Car c = carDao.findByIdNotLive(p.getParkedCarId());
                //generate pending intent for alarm
                PendingIntent pendingIntent = AlarmUtility.generatePendingIntent(p.getAlarmTime(),
                        this, p, c);

                //set alarm
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, p.getAlarmTime() - FIVE_MIN_IN_MILLIS,
                        pendingIntent);
            }
        }

        notificationManager.cancel(NOTIFICATION_ID);
        stopSelf();
    }
}