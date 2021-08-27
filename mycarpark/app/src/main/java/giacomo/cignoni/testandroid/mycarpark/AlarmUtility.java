package giacomo.cignoni.testandroid.mycarpark;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
/*
Contains the methods which regulate the alarm for a specific park, including showing the dialog
NOTE: all hour is expressed in 24h format
 */
public class AlarmUtility implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static String NOTIFICATION_CHANNEL_ID = "mycarpark_notification_channel";
    private static String NOTIFICATION_EXTRA_TEXT = "notification_extra_text";
    private static String NOTIFICATION_EXTRA_ID = "notification_extra_id";
    private static String NOTIFICATION_EXTRA_CAR_ID = "notification_extra_car_id";
    public static long FIVE_MIN_IN_MILLIS = 300000;


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

        //initializes datepicker
        Calendar c = Calendar.getInstance();
        int initialYear = c.get(Calendar.YEAR);
        int initialMonth = c.get(Calendar.MONTH);
        int initialDay = c.get(Calendar.DAY_OF_MONTH);
        datePickerDialog.updateDate(initialYear, initialMonth, initialDay);
        //sets min date as park start time
        datePickerDialog.getDatePicker().setMinDate(p.getStartTime());
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
        long timeInMillis = MainActivity.dateToMillis(selectedYear, selectedMonth, selectedDay, hour, minute);
        //insert alarm time into DB
        mainActivity.getDBViewModel().setParkAlarmTime(currentPark, timeInMillis);


        //sets alarm
        PendingIntent pendingIntent = generatePendingIntent(timeInMillis, mainActivity,
                currentPark, mainActivity.getDBViewModel().getCurrentCar());
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis - FIVE_MIN_IN_MILLIS, pendingIntent);
    }

    /*
    Creates PendingIntent
     */
    public static PendingIntent generatePendingIntent(long timeInMillis, Context context, Park p, Car c) {
        String notificationText = "Park for car "+c.getName()+
                " is expiring at "+MainActivity.getDateFromMillis(timeInMillis, "HH:mm");
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(NOTIFICATION_EXTRA_TEXT, notificationText);
        intent.putExtra(NOTIFICATION_CHANNEL_ID, (int) p.getParkId());
        intent.putExtra(NOTIFICATION_EXTRA_CAR_ID, p.getParkedCarId());
        return PendingIntent.getBroadcast(context, (int) p.getParkId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

     /*
     Removes alarm from database and the PendingIntent set with AlarmManager
     */
    public void removeAlarm(Park p) {
        this.currentPark = p;

        //removes alarm
        PendingIntent pendingIntent = generatePendingIntent(currentPark.getAlarmTime(), mainActivity,
                currentPark, mainActivity.getDBViewModel().getCurrentCar());
        AlarmManager alarmManager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        //reset alarm in DB
        mainActivity.getDBViewModel().setParkAlarmTime(currentPark, 0);
    }

    /*
    Receiver for park alarm
     */
    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("mytag", "onReceive: received alarm, ready to notify");


            //create an explicit intent to launch mainActivity with parked car id as extra
            Intent onTapIntent = new Intent(context, MainActivity.class);
            onTapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            onTapIntent.putExtra(context.getString(R.string.start_car_extra_intent),
                    intent.getLongExtra(NOTIFICATION_EXTRA_CAR_ID, 0));

            PendingIntent onTapPendingIntent = PendingIntent.getActivity(context, 0, onTapIntent, 0);


            //builds notification
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                            .setContentTitle("park expired")
                            .setContentText(intent.getStringExtra(NOTIFICATION_EXTRA_TEXT))
                            .setSmallIcon(R.drawable.ic_notification_car_24)
                            .setAutoCancel(true)
                            .setContentIntent(onTapPendingIntent);



            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(intent.getIntExtra(NOTIFICATION_EXTRA_ID,0),
                    notificationBuilder.build());
        }

    }

    /*
    Boot receiver, launches service to restore alarms
     */
    public static class BootCompletedReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Intent i = new Intent(context, RestoreAlarmsService.class);
                //use startForegroundService if API level >= 26
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(i);
                } else {
                    context.startService(i);
                }
            }
        }
    }

}
