package giacomo.cignoni.testandroid.mycarpark;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.chip.Chip;

import java.util.Calendar;
/*
Contains the methods which regulate the alarm for a specific park, including showing the dialog
NOTE: all hour is expressed in 24h format
 */
public class AlarmUtility implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static String NOTIFICATION_CHANNEL_ID = "mycarpark_notification_channel";
    private static String NOTIFICATION_EXTRA_TEXT = "notification_extra_text";
    private static String NOTIFICATION_EXTRA_ID = "notification_extra_id";


    private MainActivity mainActivity;
    private Park currentPark;

    //saved time and date parameters that have been selected
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private TimePickerDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;

    public AlarmUtility(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        Calendar c = Calendar.getInstance();
        int initialYear = c.get(Calendar.YEAR);
        int initialMonth = c.get(Calendar.MONTH);
        int initialDay = c.get(Calendar.DAY_OF_MONTH);
        timePickerDialog = new TimePickerDialog(this.mainActivity, this, 0, 0, true);
        datePickerDialog = new DatePickerDialog(this.mainActivity, this, initialYear, initialMonth, initialDay);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mainActivity.getString(R.string.notification_channel_name);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            // Register the channel with the system
            NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /*
    Shows datePicker dialog initialized with current date
     */
    public void showDialog(Park p) {
        this.currentPark = p;

        Calendar c = Calendar.getInstance();
        int initialYear = c.get(Calendar.YEAR);
        int initialMonth = c.get(Calendar.MONTH);
        int initialDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog.updateDate(initialYear, initialMonth, initialDay);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //saves date params
        selectedYear = year;
        selectedDay = day;
        selectedMonth = month;

        //initializes with current time and shows timePicker dialog
        Calendar c = Calendar.getInstance();
        int initialHour = c.get(Calendar.HOUR_OF_DAY);
        int initialMinute = c.get(Calendar.MINUTE);
        timePickerDialog.updateTime(initialHour, initialMinute);
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        //calculate selected date in millis
        long timeInMillis = this.dateToMillis(selectedYear, selectedMonth, selectedDay, hour, minute);
        //insert alarm time into DB
        mainActivity.getDBViewModel().setParkAlarmTime(currentPark, timeInMillis);


        //sets alarm
        PendingIntent pendingIntent = generatePendingIntent(timeInMillis);
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    /*
     Creates PendingIntent
     */
    private PendingIntent generatePendingIntent(long timeInMillis) {
        String notificationText = "Park for car "+mainActivity.getDBViewModel().getCurrentCar().getName()+
                " is expiring at "+MainActivity.getDate(timeInMillis, "HH:mm");
        Intent intent = new Intent(mainActivity, AlarmReceiver.class);
        intent.putExtra(NOTIFICATION_EXTRA_TEXT, notificationText);
        intent.putExtra(NOTIFICATION_CHANNEL_ID, (int) currentPark.getParkId());
        return PendingIntent.getBroadcast(mainActivity, (int) currentPark.getParkId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

     /*
    Removes alarm form database and the PendingIntent set with AlarmManager
     */
    public void removeAlarm(Park p) {
        this.currentPark = p;

        //removes alarm
        PendingIntent pendingIntent = generatePendingIntent(currentPark.getAlarmTime());
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        //reset alarm in DB
        mainActivity.getDBViewModel().setParkAlarmTime(currentPark, 0);
    }

    /*
    Transforms int for year, month, day, hour and minute in millis from epoch
     */
    public static long dateToMillis(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTimeInMillis();
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("mytag", "onReceive: received alarm, ready to notify");

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("park expired")
                            .setContentText(intent.getStringExtra(NOTIFICATION_EXTRA_TEXT))
                            .setSmallIcon(R.drawable.ic_notification_car_24);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(intent.getIntExtra(NOTIFICATION_EXTRA_ID,0),
                    notificationBuilder.build());
        }

    }

}
