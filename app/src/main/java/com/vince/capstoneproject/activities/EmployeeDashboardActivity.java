package com.vince.capstoneproject.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.Interfaces.AsyncIfComplete;
import com.vince.capstoneproject.Interfaces.AsyncResponse;
import com.vince.capstoneproject.Interfaces.NotesCallback;
import com.vince.capstoneproject.R;
import com.vince.capstoneproject.RecyclerViewAdapter;
import com.vince.capstoneproject.SwipeToDeleteCallback;
import com.vince.capstoneproject.database.AccessDatabaseTask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This is the main activity that we're calling Employee Dashboard. This dashboard is responsible
 * for a few things. First of all, it allows the user to clock in and out as long as the date that
 * they selected in the Calendar Activity is today's date. If it isn't, they have to navigate to
 * the Add Job activity where they can specify a date and clock in/out time for the job to add.
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class EmployeeDashboardActivity extends AppCompatActivity
        implements AsyncResponse,
        AsyncIfComplete,
        DatePickerDialog.OnDateSetListener,
        NotesCallback<ArrayList<String>>,
        TabLayout.OnTabSelectedListener {

    private static final int ADD_JOB_REQUEST = 1;
    private final String TAG = "TAG";
    private AlertDialog alertDialog;
    private ArrayList<ArrayList<LocalDateTime>> temps;
    private Employee tempEmployee;
    private Chronometer chronometer;
    private boolean running;
    private LocalDateTime selectedDate;
    private Employee employee;
    private TextView dateView;
    private LocalDateTime todayDate;
    private TabLayout tabLayout;
    public ArrayList<String> usernames;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private ConstraintLayout constraintLayout;
    private ArrayList<ArrayList<LocalDateTime>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);


        recyclerView = findViewById(R.id.recyclerView);
        constraintLayout = findViewById(R.id.constraintLayout);
        enableSwipeToDeleteAndUndo();

        if (Employee.username.equalsIgnoreCase("admin")) {
            /*
            populate tabLayout with all employee names, dates, times
             */
            AccessDatabaseTask getUsernames =
                    new AccessDatabaseTask(AccessDatabaseTask.Operation.SELECT_USERNAMES);
            getUsernames.notesCallback = this;
            getUsernames.execute(getApplicationContext());
        }

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(Employee.username));
        tabLayout.addOnTabSelectedListener(this);

        todayDate = LocalDateTime.now();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("Employee")) {
            employee = (Employee) extras.get("Employee");
        }

        temps = new ArrayList<>();

        View welcome_view = findViewById(R.id.dashboard_welcome);
        welcome_view.setFocusable(false); //don't want to be able to exit the Welcome text

        /*
        Want to make sure the keyboard is closed when entering this activity since
        there is no text input required and we want to be able to see all elements.
         */
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(CreateAccountActivity.INPUT_METHOD_SERVICE);
        View view = new View(this);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        dateView = findViewById(R.id.todaysDate);

        Button clockInButton = findViewById(R.id.clockInButton);
        Button clockOutButton = findViewById(R.id.clockOutButton);
        clockOutButton.setVisibility(View.INVISIBLE);

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("Total time clocked in: %s");
        chronometer.setVisibility(View.INVISIBLE);

        clockInButton.setOnClickListener(event -> {
            if (!isSelectedDateToday(selectedDate)) {
                Toast.makeText(this, "Must select today's date to clock in",
                        Toast.LENGTH_LONG).show();
            } else {
                alertDialog = new AlertDialog.Builder(this).create();
                tempEmployee = (Employee) getIntent().getExtras().get("Employee");
                alertDialog.setTitle("Confirm Clock In");
                alertDialog.setMessage("Are you sure you want to clock in?");

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which)
                        -> dialog.cancel());

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Clock In", (dialog, which)
                        -> {
                    tempEmployee.setClockInTime(LocalDateTime.now());
                    Toast.makeText(this, "Clocked in", Toast.LENGTH_LONG).show();

                    //show the timer and the clock out button
                    chronometer.setVisibility(View.VISIBLE);
                    clockOutButton.setVisibility(View.VISIBLE);
                    //Starts timer displaying how long the user has been clocked in
                    startChronometer();
                });
                alertDialog.show();
            }
        });

        clockOutButton.setOnClickListener(event -> {
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Confirm Clock Out");
            alertDialog.setMessage("Are you sure you want to clock out?");
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which)
                    -> dialog.cancel());
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Clock Out", (dialog, which)
                    -> {
                //hide clock out button when clocking out
                clockOutButton.setVisibility(View.INVISIBLE);

                tempEmployee.setClockOutTime(LocalDateTime.now());
                Toast.makeText(this, "Clocked out", Toast.LENGTH_LONG).show();

                ArrayList<LocalDateTime> dateTemps = new ArrayList<>();
                dateTemps.add(tempEmployee.getClockOutTime());
                dateTemps.add(tempEmployee.getClockInTime());

                temps.add(dateTemps);
                mAdapter.setData(temps);
                mAdapter.notifyDataSetChanged();
                populateListViewDates(temps);

                AccessDatabaseTask insertTimes = new AccessDatabaseTask(tempEmployee,
                        AccessDatabaseTask.Operation.INSERT_TIMES);
                insertTimes.execute(this);

                resetChronometer();
                chronometer.setVisibility(View.INVISIBLE);
            });
            alertDialog.show();
        });

        /*
         * To add a job to the database, we need to get some info from the user first.
         * First thing we need is the date for which they want to add the job.
         * Next, the clock in and clock out times. These are retrieved from the user in the
         * AddJobActivity.
         */
        Button addJob = findViewById(R.id.add_job_button);
        addJob.setOnClickListener(v -> {
            Intent i = new Intent(EmployeeDashboardActivity.this,
                    AddJobActivity.class);
            startActivityForResult(i, ADD_JOB_REQUEST);
        });
    }

    /**
     * Called when returning to this activity from an activity that has been started by
     * {@link android.app.Activity#startActivityForResult(Intent, int)}
     *
     * @param requestCode a pre-defined request code
     * @param resultCode  a pre-defined result code that returns
     *                    {@link android.app.Activity#RESULT_OK} or {@link android.app.Activity#RESULT_CANCELED}
     * @param data        the Intent that was used to return to this activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras;

        if (data != null) {
            extras = data.getExtras();

            switch (requestCode) {
                case ADD_JOB_REQUEST:
                    if (resultCode == RESULT_OK) {
                        LocalDateTime new_clockInDate = (LocalDateTime) extras.get("clockInDate");
                        LocalDateTime new_clockOutDate = (LocalDateTime) extras.get("clockOutDate");
                        String notes = (String) extras.get("notes");

                        employee.setNotes(notes);
                        employee.setClockInTime(new_clockInDate);
                        employee.setClockOutTime(new_clockOutDate);

                        AccessDatabaseTask insertNewDate = new AccessDatabaseTask(employee,
                                AccessDatabaseTask.Operation.INSERT_TIMES);
                        insertNewDate.execute(getApplicationContext());
                        insertNewDate.completionCode = this;

                        AccessDatabaseTask reselectTimes = new AccessDatabaseTask(employee,
                                AccessDatabaseTask.Operation.SELECT_TIMES, new_clockInDate);
                        reselectTimes.execute(getApplicationContext());
                        reselectTimes.response = this;
                    }
                    break;
            }
        }


    }

    /**
     * This method is responsible for displaying text in a TextView that shows the user which
     * date they've selected.
     *
     * @param view the TextView that displays the date that the user has currently selected
     */
    private void setDate(TextView view, LocalDateTime todaysDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yyyy", Locale.US);
        StringBuilder sb = new StringBuilder();
        sb.append("Today's date: ");
        sb.append(System.getProperty("line.separator"));

        if (todaysDate == null) {
            LocalDateTime today = LocalDateTime.now();
            sb.append(formatter.format(today));
        } else {
            sb.append(formatter.format(todaysDate));
        }

        view.setText(sb.toString());
        view.setFocusable(false);
    }

    /**
     * Adds view objects with the the layout specified in the resources layout folder as:
     *
     *
     * @param employeeTimes the two dimensional ArrayList of Dates to add to each view object.
     *                      Each ArrayList contains a LocalDateTime where index 0 is the clock
     *                      out time and index 1 is the clock in time
     */
    private void populateListViewDates(ArrayList<ArrayList<LocalDateTime>> employeeTimes) {
        if (employeeTimes != null) {
            if(mAdapter == null) {
                mAdapter = new RecyclerViewAdapter(employeeTimes, EmployeeDashboardActivity.this);
                recyclerView.setAdapter(mAdapter);
            }
        }
    }

    /**
     * Starts the timer showing the user how long they've been clocked in for
     */
    public void startChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    @SuppressWarnings("unused")
    public void pauseChronometer() {
        if (running) {
            chronometer.stop();
            running = false;
        }
    }

    /**
     * Resets the timer that shows the user how long they've been clocked in for.
     */
    public void resetChronometer() {
        if (running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.stop();
            running = false;
        }
    }

    /**
     * Checks to see if the date passed into this method is the same as today's date.
     *
     * @param selectedDate the date passed in to check if it matches today's date
     * @return true if the {@param selectedDate} is today's date. False if it isn't
     */
    private boolean isSelectedDateToday(LocalDateTime selectedDate) {
        if(selectedDate == null)
            return false;
        LocalDate selectedDateLD = LocalDate.of(selectedDate.getYear(), selectedDate.getMonthValue(),
                selectedDate.getDayOfMonth());
        LocalDate now = LocalDate.now();
        return now.equals(selectedDateLD);
    }

    /**
     * Called from AccessDatabaseTask in onPostExecute in order to pass the result set back to this
     * class
     *
     * Implemented from the {@link AsyncResponse} interface.
     *
     * @param output the result from the AccessDatabaseTask
     */
    @Override
    public void processFinish(ArrayList<ArrayList<LocalDateTime>> output) {
        this.temps = output;

        populateListViewDates(output);
        if(mAdapter != null) {
            mAdapter.setData(output);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Inserted new date", Toast.LENGTH_LONG).show();
    }

    /**
     * Callback when a date is picked using a {@link DatePickerDialog}
     *
     * @param view       the {@link DatePickerDialog}
     * @param year       the selected year
     * @param month      the selected month
     * @param dayOfMonth the selected day of the month
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        selectedDate = LocalDateTime.of(LocalDate.of(year, month + 1, dayOfMonth),
                LocalTime.now());
        setDate(dateView, selectedDate);

        todayDate = LocalDateTime.of(LocalDate.of(year, month + 1, dayOfMonth),
                LocalTime.now());

        AccessDatabaseTask getDatesForListView = new AccessDatabaseTask(employee,
                AccessDatabaseTask.Operation.SELECT_TIMES, selectedDate);
        getDatesForListView.execute(getApplicationContext());
        getDatesForListView.response = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Called when the button in the action bar is clicked.
     *
     * @param item the item that is clicked on the action bar
     * @return see {@link android.app.TimePickerDialog}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_bar_calendar_button) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                    todayDate.getYear(),
                    todayDate.getMonthValue() - 1,
                    todayDate.getDayOfMonth());
            datePickerDialog.show();
            Log.e(TAG, todayDate.getMonthValue() + "");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Adds a tab for each user that exists in the database. This is called from
     * {@link #passNotes(ArrayList)} which is only called if the currently logged in user is
     * "admin"
     */
    private void populateTabLayout() {
        for (String s : usernames) {
            if (!s.equalsIgnoreCase("admin"))
                tabLayout.addTab(tabLayout.newTab().setText(s));
        }
    }

    /**
     * The implementation of the {@link NotesCallback}
     *
     * @param note
     */
    @Override
    public void passNotes(ArrayList<String> note) {
        usernames = note;
        populateTabLayout();
    }

    /**
     * Called when a tab is selected
     *
     * @param tab the tab that is selected
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (selectedDate == null) {
            alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Date error");
            alertDialog.setMessage("You need to select a date first. Click the calendar icon.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Okay", ((dialog, which) -> {
                dialog.cancel();
            }));
            alertDialog.show();
        } else {
            Employee.username = tab.getText() + "";

            AccessDatabaseTask getTimes =
                    new AccessDatabaseTask(AccessDatabaseTask.Operation.SELECT_TIMES, selectedDate);
            getTimes.response = this;
            getTimes.execute(getApplicationContext());
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    /**
     * This method is responsible for implementing the callback that is called when the user
     * swipes on the RecyclerView that holds the job information.
     */
    private void enableSwipeToDeleteAndUndo() {

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            // Called when the user swipes on one of the RecyclerViews
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                alertDialog = new AlertDialog.Builder(EmployeeDashboardActivity.this)
                        .create();
                alertDialog.setMessage("Delete entry?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", ((dialog, which) -> {
                    Employee employee = new Employee();

                    employee.setClockInTime(temps.get(position)
                            .get(1));

                    mAdapter.removeItem(position);
                    mAdapter.notifyDataSetChanged();
                    temps.remove(position);

                    AccessDatabaseTask deleteTime = new AccessDatabaseTask(employee,
                            AccessDatabaseTask.Operation.DELETE, selectedDate);
                    deleteTime.execute(EmployeeDashboardActivity.this);

                    Snackbar snackbar = Snackbar
                            .make(constraintLayout, "Item was removed from the list.",
                                    Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", view -> {

                        mAdapter.restoreItem();
                        recyclerView.scrollToPosition(position);
                    });

                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();

                }));
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        ((dialog, which) -> {
                    alertDialog.cancel();
                    mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                }));
                alertDialog.show();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }
}
