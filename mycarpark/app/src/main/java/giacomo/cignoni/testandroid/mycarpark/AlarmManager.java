package giacomo.cignoni.testandroid.mycarpark;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class AlarmManager implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private Park currentPark;
    //saved time and date parameters that have been selected
    private int selectedDay, selectedMonth, selectedYear, selectedHour, selectedMinute;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;

    public AlarmManager(MainActivity mainActivity) {
        Calendar c = Calendar.getInstance();
        int initialYear = c.get(Calendar.YEAR);
        int initialMonth = c.get(Calendar.MONTH);
        int initialDay = c.get(Calendar.DAY_OF_MONTH);
        timePickerDialog = new TimePickerDialog(mainActivity, this, 0, 0, true);
        datePickerDialog = new DatePickerDialog(mainActivity, this, initialYear, initialMonth, initialDay);

    }

    /*
    Shows datePicker dialog initialized with current date
     */
    public void showDialog() {
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
        selectedHour = hour;
        selectedMinute = minute;

        //TODO: set alarm
    }

    public void setCurrentPark (Park park){
        this.currentPark = park;
    }


}
