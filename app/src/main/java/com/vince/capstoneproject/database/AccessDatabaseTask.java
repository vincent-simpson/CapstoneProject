package com.vince.capstoneproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.Interfaces.AsyncIfComplete;
import com.vince.capstoneproject.Interfaces.AsyncIfNotComplete;
import com.vince.capstoneproject.Interfaces.AsyncResponse;
import com.vince.capstoneproject.Interfaces.NotesCallback;
import com.vince.capstoneproject.activities.CreateAccountActivity;
import com.vince.capstoneproject.activities.EmployeeDashboardActivity;
import com.vince.capstoneproject.database.EmployeeDatabaseContract.EmployeeData;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * The purpose of this class is to handle all of the work required to access the SQLite database
 * and manage all of its data. AsyncTask is used to run this class in the background on the UI thread
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 */
public class AccessDatabaseTask extends AsyncTask<Context, Void, ArrayList<ArrayList<LocalDateTime>>> {

    //Defining all of the possible operations we could be performing
    public enum Operation {
        SELECT, INSERT, UPDATE, DELETE, INSERT_TIMES, SELECT_TIMES, SELECT_NOTES, SELECT_USERNAMES
    }

    public AsyncResponse response = null;
    public AsyncIfComplete completionCode;
    public AsyncIfNotComplete failureCode;
    public WeakReference<EmployeeDashboardActivity> employeeDashboardActivity;
    public NotesCallback notesCallback = null;
    private Operation OPERATION;
    private Employee employee;
    private ArrayList<ArrayList<LocalDateTime>> employeeResults = new ArrayList<ArrayList<LocalDateTime>>();
    private SQLiteDatabase db_read;
    private SQLiteDatabase db_write;
    private EmployeeDataDbHelper employeeDataDbHelper;
    private LocalDateTime selectedDate;
    private String notes;
    private ArrayList<String> usernames;

    public AccessDatabaseTask(WeakReference<EmployeeDashboardActivity> employeeDashboardActivity) {
        this.employeeDashboardActivity = employeeDashboardActivity;
    }

    /**
     * Constructs an AccessDatabaseTask object with the specified Employee object and the Operation
     * to perform
     *
     * @param employee  to attach to this AccessDatabaseTask
     * @param operation to perform
     */
    public AccessDatabaseTask(Employee employee, Operation operation) {
        this.OPERATION = operation;
        this.employee = employee;
    }

    /**
     * Constructs an AccessDatabaseTask object with the specified Employee object, Operation to
     * perform, and a Date to store as the currently selected date from the CalendarActivity
     *
     * @param employee     to attach to this AccessDatabaseTask
     * @param operation    to perform
     * @param selectedDate current date selected from CalendarActivity
     */
    public AccessDatabaseTask(Employee employee, Operation operation, LocalDateTime selectedDate) {
        this.OPERATION = operation;
        this.employee = employee;
        this.selectedDate = selectedDate;
    }

    @SuppressWarnings("unused")
    public AccessDatabaseTask(Operation operation) {
        this.OPERATION = operation;
    }

    public AccessDatabaseTask(Operation operation, LocalDateTime selectedDate) {
        this.OPERATION = operation;
        this.selectedDate = selectedDate;
    }

    @SuppressWarnings("unused")
    public ArrayList<ArrayList<LocalDateTime>> getEmployeeResults() {
        return employeeResults;
    }

    @Override
    protected ArrayList<ArrayList<LocalDateTime>> doInBackground(Context... contexts) {
        if (employeeDataDbHelper == null)
            employeeDataDbHelper = new EmployeeDataDbHelper(contexts[0]);
        if (db_read == null)
            db_read = employeeDataDbHelper.getReadableDatabase();
        if (db_write == null)
            db_write = employeeDataDbHelper.getWritableDatabase();

        switch (OPERATION) {
            //If we want to select all of the clock in and out dates associated with the current
            //logged in user
            case SELECT_TIMES:
                createTimesView();
                employeeResults = getDates(selectedDate);
                break;
            case SELECT_NOTES:
                notes = getNotes(selectedDate);
                break;
            case SELECT_USERNAMES:
                usernames = getUsernames();
                break;
            case SELECT:
                runSelectStatement();
                break;
            case INSERT:
                runInsertEmployeeStatement(employee);
                break;
            case INSERT_TIMES:
                runTimeInsertStatement(employee);
                break;
            case UPDATE:
                runUpdateStatement();
                break;
            case DELETE:
                deleteDateEntry(employee.getClockInTime());
                break;
        }
        return employeeResults;
    }

    @Override
    protected void onPostExecute(ArrayList<ArrayList<LocalDateTime>> employeeResults) {
        if (response != null) {
            response.processFinish(employeeResults);
        }
        //This could be null if the username, password, or name fields in CreateAccountActivity
        //are invalid.
        if (completionCode != null) {
            completionCode.onComplete();
        }

        if (notesCallback != null && notes != null) {
            notesCallback.passNotes(notes);
        }

        if (notesCallback != null && usernames != null) {
            notesCallback.passNotes(usernames);
        }

        db_read.close();
        db_write.close();
    }

    /**
     * This method is responsible for running a select statement on the database. This query
     * returns all of the columns and all of the data in the database. It is equivalent to a
     * statement such as "SELECT * FROM [table_name]"
     */
    private void runSelectStatement() {
        String[] projection = {
                EmployeeData.ID,
                EmployeeData.FIRST_NAME,
                EmployeeData.LAST_NAME,
                EmployeeData.USERNAME,
                EmployeeData.PASSWORD
        };

        Cursor cursor = db_read.query(
                EmployeeData.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        //The username is set statically in MainActivity, so not needed here.
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String firstName_result = cursor.getString(1);
            String lastName_result = cursor.getString(2);
            String password_result = cursor.getString(4);
            employee = new Employee(id, firstName_result, lastName_result, password_result);
        }
        cursor.close();
    }

    /**
     * Queries the database to find the userID of the currently logged in user.
     *
     * @return a string with the userID.
     */
    private String getUserID() {
        String[] projection = {
                BaseColumns._ID
        };

        String employeeUsername = Employee.username;

        String selection = EmployeeData.USERNAME + " = ?";
        String[] selectionArgs = {employeeUsername + ""};

        Cursor cursor = db_read.query(
                EmployeeData.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        int userID;
        if (cursor.moveToNext()) {
            userID = cursor.getInt(cursor.getColumnIndexOrThrow(EmployeeData.ID));
            cursor.close();
        } else {
            throw new NullPointerException("Cursor is null");
        }
        return userID + "";
    }

    /**
     * Inserts the specified employee into the database. This process will begin in the
     * CreateAccountActivity activity.
     *
     * @param employee the employee to insert into the database.
     * @throws SQLiteConstraintException if there is a duplicate entry. Meaning, the user is trying
     *                                   to create a username that already exists. If this is the
     *                                   case, then we log the exception.
     */
    private void runInsertEmployeeStatement(Employee employee) throws SQLiteConstraintException {
        ContentValues values = new ContentValues();
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();
        String username = Employee.username;
        String password = employee.getPassword();

        values.put(EmployeeData.FIRST_NAME, firstName);
        values.put(EmployeeData.LAST_NAME, lastName);
        values.put(EmployeeData.USERNAME, username);
        values.put(EmployeeData.PASSWORD, password);

        try {
            long insert = db_write.insertOrThrow(EmployeeData.TABLE_NAME,
                    null, values);
        } catch (SQLiteConstraintException e) {
            Log.e("SQLiteConstraintExc", "SQLiteConstraintException thrown: " + e);
        }
    }

    /**
     * Inserts into the "employee_times" table the clock in and out times for each user.
     *
     * @param employee the data to be inserted into the database
     */
    private void runTimeInsertStatement(Employee employee) {
        String clockInTime = employee.getClockInTime().toString();
        String clockOutTime = employee.getClockOutTime().toString();
        String employee_data_ID = getUserID();
        String notes = employee.getNotes();

        ContentValues values = new ContentValues();
        values.put(EmployeeDatabaseContract.EmployeeTimes.CLOCK_IN_TIME, clockInTime);
        values.put(EmployeeDatabaseContract.EmployeeTimes.CLOCK_OUT_TIME, clockOutTime);
        values.put(EmployeeDatabaseContract.EmployeeTimes.EMPLOYEE_DATA_ID, employee_data_ID);
        values.put(EmployeeDatabaseContract.EmployeeTimes.NOTES, notes);

        try {
            long insert = db_write.insertOrThrow(EmployeeDatabaseContract.EmployeeTimes.TABLE_NAME,
                    null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the view in the database that contains the employee_times and the employee_data
     * tables joined, only displaying the dates that have the currently logged-in user as their
     * userID
     */
    private void createTimesView() {
        String deleteView = "DROP VIEW IF EXISTS time_join_view";
        db_write.execSQL(deleteView);
        String createView = "CREATE VIEW IF NOT EXISTS time_join_view AS " +
                "SELECT * FROM employee_data ed " +
                "JOIN employee_times et ON ed._id = et.employee_data_id " +
                "WHERE et.employee_data_id = " + getUserID();
        db_write.execSQL(createView);
    }

    /**
     * This method is responsible for getting the dates from the time_join_view created in
     * {@link #createTimesView()}. The dates that are retrieved are dates on the same day, month,
     * and year as the {@link #selectedDate}. This makes sense, since the dates we want to populate
     * the ListView with should be on the same day, month, and year as the date that the user
     * selected in CalendarActivity.
     *
     * @return a two dimensional ArrayList where each primary index is an ArrayList consisting of
     * the clock out time (at index 0) and clock in time (at index 1).
     */
    private ArrayList<ArrayList<LocalDateTime>> getDates(LocalDateTime associationDate) {
        ArrayList<ArrayList<LocalDateTime>> dates = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFormatted = dateTimeFormatter.format(associationDate);

//        String selection = EmployeeDatabaseContract.EmployeeTimes.CLOCK_IN_TIME + " = ?";
//        String[] selectionArgs = { df2.format(selectedDate) };

        Cursor cursor = db_read.rawQuery("SELECT * FROM time_join_view " +
                "WHERE clock_in LIKE '" + dateFormatted + "%'", null);
        while (cursor.moveToNext()) {
            LocalDateTime clockInTime = LocalDateTime.parse(
                    cursor.getString(cursor.getColumnIndexOrThrow("clock_in"))
            );
            LocalDateTime clockOutTime = LocalDateTime.parse(
                    cursor.getString(cursor.getColumnIndexOrThrow("clock_out"))
            );

            if (employee != null) {
                employee.setClockInTime(clockInTime);
                employee.setClockOutTime(clockOutTime);
            }

            ArrayList<LocalDateTime> temp = new ArrayList<>();
            temp.add(clockOutTime);
            temp.add(clockInTime);
            dates.add(temp);
        }
        cursor.close();
        return dates;
    }

    /**
     * Currently unused.
     */
    @SuppressWarnings("unused")
    private void runUpdateStatement() {
    }

    /**
     * Currently unused.
     *
     * @param usernameToDelete the username to delete
     */
    @SuppressWarnings("unused")
    private void deleteUser(String usernameToDelete) {
        String selection = EmployeeData.USERNAME + " LIKE ?";
        String[] selectionArgs = {usernameToDelete};

        int numOfRowsDeleted = db_write.delete(EmployeeData.TABLE_NAME, selection, selectionArgs);

    }

    /**
     * Deleted from the database any row that has the same exact date as the parameter.
     *
     * @param date the date to be deleted.
     */
    private void deleteDateEntry(LocalDateTime date) {
        String selection = EmployeeDatabaseContract.EmployeeTimes.CLOCK_IN_TIME + " LIKE ?";
        String[] selectionArgs = {date.toString()};
        int deletedRows = db_write.delete(EmployeeDatabaseContract.EmployeeTimes.TABLE_NAME,
                selection,
                selectionArgs);
        createTimesView();
        /*
        createTimesView() is called again to recreate (update) the times_join_view because an entry
        was deleted.
         */
        //Since an item was deleted, need to update the global variable to reflect that
        employeeResults = getDates(date);
    }

    /**
     * Gets the notes from the database entry that matches the date parameter.
     *
     * @param date the date to get the notes from
     * @return the notes of the job as a String
     */
    public String getNotes(LocalDateTime date) {
        StringBuilder result = new StringBuilder();
        Cursor cursor = db_read.rawQuery("SELECT notes FROM time_join_view " +
                "WHERE clock_in LIKE '" + date.toString() + "%'", null);

        while (cursor.moveToNext()) {
            result.append(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        }
        cursor.close();
        System.out.println("GET NOTES TO STRING: " + result.toString());
        return result.toString();
    }

    /**
     * Called if there is an SQLException thrown.
     * Close the databases and run the method in {@link CreateAccountActivity#onFailure()}
     *
     * @param employeeResults not used
     */
    @Override
    protected void onCancelled(ArrayList<ArrayList<LocalDateTime>> employeeResults) {
        db_write.close();
        db_read.close();
        if (failureCode != null) failureCode.onFailure();
    }

    @SuppressWarnings("unused")
    public String getNotes() {
        return notes;
    }

    /**
     * This method runs a query on the database that returns all usernames in the database.
     *
     * @return an ArrayList of Strings that contain all of the usernames in the database;
     */
    private ArrayList<String> getUsernames() {
        ArrayList<String> usernames = new ArrayList<>();
        Cursor cursor = db_read.rawQuery("SELECT DISTINCT username FROM employee_data " +
                "ed JOIN employee_times et ON ed._id = et.employee_data_id", null);
        int colIndex = cursor.getColumnIndexOrThrow("username");

        while (cursor.moveToNext()) {
            usernames.add(cursor.getString(colIndex));
        }
        return usernames;
    }
}
