package com.vince.capstoneproject.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.R;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

/**
 * This activity is responsible for displaying both the DatePicker and the two TimePickers
 * that are being used to get the new date that the user wants to insert.
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class AddJobActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {

    int year = 0, month = 0, day = 0, hour = 0, minute = 0;
    private boolean am;
    private Button setNewDate, setNewClockInTime, setNewClockOutTime;
    private String notes;
    private boolean isClockInTime;
    private LocalDateTime newClockInTime;
    private LocalDateTime newClockOutTime;
    private boolean newClockInButtonClicked = false, newClockOutButtonClicked = false;
    private LocalDateTime now;
    private LocalDateTime nowPlusOneHour;
    private Employee employee;
    private String callingClass = "";
    private AlertDialog alertDialog;
    boolean isInvalidDate = false;
    private DateTimeFormatter timeFormatter;
    private EditText notesET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);

        setNewDate = findViewById(R.id.selectNewDateButton);
        setNewClockOutTime = findViewById(R.id.selectNewClockOutTimeButton);

        notesET = findViewById(R.id.notesEditText);
        setNewClockInTime = findViewById(R.id.selectNewClockInTimeButton);
        Button finishAddJob = findViewById(R.id.finish_add_job);

        //dateFormatter output example: Sun, Mar 10
        DateTimeFormatter dateFormatter = DateTimeFormatter
                .ofPattern("EEE, MMM dd");

        //timeFormatter output example: 12:11 PM
        timeFormatter = DateTimeFormatter
                .ofPattern("h:mm");

        now = LocalDateTime.now();
        year = now.getYear();
        month = now.getMonthValue();
        day = now.getDayOfMonth();
        hour = now.getHour();
        minute = now.getMinute();
        am = (hour < 12);

        nowPlusOneHour = now.plusHours(1);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            callingClass = (String) extras.get("callingClass");

            if (callingClass != null) {
                switch (callingClass) {
                    case "RecyclerViewAdapter": //if we're coming from RecyclerViewAdapter
                        notes = (String) extras.get("notes");
                        employee = (Employee) extras.get("employee");
                        setNewDate.setText(dateFormatter.format(employee.getClockInTime()));
                        setNewClockInTime.setText(timeFormatter.format(employee.getClockInTime()));
                        setNewClockOutTime.setText(timeFormatter.format(employee.getClockOutTime()));
                        notesET.setText(notes);

                        setNewDate.setEnabled(false);
                        setNewClockInTime.setEnabled(false);
                        setNewClockOutTime.setEnabled(false);
                        notesET.setEnabled(false);

                        break;

                    case "Dashboard" :
                        LocalDateTime l = (LocalDateTime) extras.get("selectedDate");
                        this.setNewDate.setText(dateFormatter.format(l));

                        newClockInTime = LocalDateTime.of(l.toLocalDate(), LocalTime.now());
                        newClockOutTime = newClockInTime.plusHours(1);

                        break;
                }
            }
        } else {

            String newText = dateFormatter.format(now);
            setNewDate.setText(newText);

            newClockInTime = now;
            newClockOutTime = nowPlusOneHour;

        }

        String newClockOutText = timeFormatter.format(nowPlusOneHour) +
                ((nowPlusOneHour.getHour() < 12) ? " AM" : " PM");
        setNewClockOutTime.setText(newClockOutText);

        String newClockInText = timeFormatter.format(now) + ((now.getHour() < 12) ? " AM" : " PM");
        setNewClockInTime.setText(newClockInText);


        setNewDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                    year, month, day);
            datePickerDialog.show();
        });

        setNewClockInTime.setOnClickListener(v -> {
            TimePickerDialog clockInTimePicker = new TimePickerDialog(this, this,
                    hour, minute, false);

            clockInTimePicker.show();
            isClockInTime = true;
            newClockInButtonClicked = true;
        });

        setNewClockOutTime.setOnClickListener(v -> {
            TimePickerDialog clockOutTimePicker = new TimePickerDialog(this, this,
                    hour, minute, false);
            clockOutTimePicker.show();
            isClockInTime = false;
            newClockOutButtonClicked = true;
        });

        finishAddJob.setOnClickListener(v -> {
            isInvalidDate = false;
                if (newClockInTime != null && newClockOutTime != null
                        && (Duration.between(newClockInTime, newClockOutTime)).isNegative()) {
                    alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Invalid Date Selection");
                    alertDialog.setMessage("Clock in time must come before clock out time");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", (dialog, which)
                            -> dialog.cancel());
                    alertDialog.show();
                    isInvalidDate = true;
                }

                if (!isInvalidDate) {
                    notes = notesET.getText().toString();

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("clockInDate", newClockInTime);
                    returnIntent.putExtra("clockOutDate", newClockOutTime);
                    returnIntent.putExtra("notes", notes);
                    setResult(EmployeeDashboardActivity.RESULT_OK, returnIntent);
                    finish();
                }

                // }
            });

    }

    /**
     * Called when the {@link DatePickerDialog} is closed after a new date has been picked
     *
     * @param view       the {@link DatePickerDialog}
     * @param year       the new year
     * @param month      the new month
     * @param dayOfMonth the new day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        setDatePickerButtonText(year, month, dayOfMonth);
    }

    /**
     * Updates the text of the button responsible for launching the {@link DatePickerDialog} based
     * on the chosen year, month, and day of month
     *
     * @param year       the selected year
     * @param month      the selected month
     * @param dayOfMonth the selected day
     */
    private void setDatePickerButtonText(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;

        //dateFormatter output example: Sun, Mar 10
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM dd", Locale.US);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        setNewDate.setText(dateFormatter.format(calendar.getTime()));
    }

    /**
     * Called when the TimePicker dialog is closed
     *
     * @param view      the TimePicker dialog
     * @param hourOfDay the selected hour, passed in 24-hour format
     * @param minute    the selected minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;

        String s = hourOfDay < 12 ? "am" : "pm";
        String hour = "";

        if (s.equalsIgnoreCase("pm") && hourOfDay != 12) {
            // subtract 12 because its in 24 hour time
            hour += hourOfDay - 12 + "";
        } else {
            hour += hourOfDay;
        }

        //If the dialog that calls this callback method originates from the new clock in button,
        //then isClockInTime will be true
        if (isClockInTime) {
            newClockInTime = LocalDate.now().atTime(hourOfDay, minute);
            Log.e("newclockintime", newClockInTime.toString());

            //If the hour is less than 12, its the AM, else its the PM
            String newClockInTimeStr = hour + ":" + minute + " " + s;
            setNewClockInTime.setText(newClockInTimeStr);
        } else {
            newClockOutTime = LocalDate.now().atTime(hourOfDay, minute);

            String newClockOutTimeStr = timeFormatter.format(newClockOutTime) + " " + s;
            setNewClockOutTime.setText(newClockOutTimeStr);
        }


    }
}
