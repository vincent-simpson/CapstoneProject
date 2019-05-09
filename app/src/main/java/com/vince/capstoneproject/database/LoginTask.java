package com.vince.capstoneproject.database;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.activities.EmployeeDashboardActivity;
import com.vince.capstoneproject.activities.MainActivity;
import com.vince.capstoneproject.database.EmployeeDatabaseContract.EmployeeData;

import java.lang.ref.WeakReference;

/**
 * The task responsible for the login process
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class LoginTask extends AsyncTask<Context, Void, Boolean> {

    private Employee employee;
    private WeakReference<Context> weakContext;
    private AlertDialog alertDialog;
    public ProgressDialog dialog;
    private SQLiteDatabase db_read;
    private SQLiteDatabase db_write;

    public LoginTask(Employee employee, MainActivity activity) {
        this.employee = employee;
        this.weakContext = new WeakReference<>(activity);
    }

    /**
     * This method executes right before {@link #doInBackground(Context...)}
     * It is responsible for showing a progress dialog when the user clicks "login"
     */
    @Override
    protected void onPreExecute() {

        dialog = new ProgressDialog(weakContext.get());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Logging you in...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        alertDialog = new AlertDialog.Builder(weakContext.get()).create();
        alertDialog.setTitle("Login Status");
    }

    /**
     * The code to execute in the background while the progress dialog is showing
     * @param contexts the context to get that is tied to the database
     * @return true if the login should be successful, false otherwise
     */
    @Override
    protected Boolean doInBackground(Context... contexts) {
        EmployeeDataDbHelper employeeDataDbHelper = new EmployeeDataDbHelper(contexts[0]);
        db_read = employeeDataDbHelper.getReadableDatabase();
        db_write = employeeDataDbHelper.getWritableDatabase();

        return attemptLogin();
    }

    /**
     * Responsible for setting the dialog message and handling the intent to start
     * {@link EmployeeDashboardActivity}
     * @param loginSuccessful true if the login was successful, false otherwise
     */
    @Override
    protected void onPostExecute(Boolean loginSuccessful) {
        if (loginSuccessful) {
            alertDialog.setMessage("Login successful");
        } else {
            alertDialog.setMessage("Login failed");
        }

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Okay", (dialog, which) -> {
            if (loginSuccessful) {
                dialog.cancel();
                Intent i = new Intent(weakContext.get(), EmployeeDashboardActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //Pass the current logged in user's username to EmployeeDashboardActivity.
                i.putExtra("Employee", employee);
                //
                weakContext.get().startActivity(i);
            } else {
                this.dialog.cancel();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            this.dialog.cancel();
        });
        alertDialog.show();
        db_read.close();
        db_write.close();
    }

    /**
     * Responsible for accessing the database in order to check to see if the username and password
     * exists in the database.
     *
     * @return true if the login was successful, false otherwise
     */
    private boolean attemptLogin() {
        String[] projection = {
                EmployeeDatabaseContract.EmployeeData.ID,
                EmployeeData.USERNAME,
                EmployeeData.PASSWORD
        };

        String selection = EmployeeData.USERNAME + " = ? AND " + EmployeeData.PASSWORD + " = ?";
        String[] selectionArgs = {Employee.username, employee.getPassword()};

        Cursor cursor = db_read.query(
                EmployeeData.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean correct = cursor.moveToNext();
        cursor.close();
        return correct;
    }

}
