package com.vince.capstoneproject.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vince.capstoneproject.database.EmployeeDatabaseContract.EmployeeData;
import com.vince.capstoneproject.database.EmployeeDatabaseContract.EmployeeTimes;

/**
 * This class is responsible for outlining the name of the database and creating the local
 * database file.
 * <p>
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 */
public class EmployeeDataDbHelper extends SQLiteOpenHelper {
    /**
     * The String representing the version of the database
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * The String representing the name of the database to be stored locally
     */
    public static final String DATABASE_NAME = "employee.db";

    private EmployeeData employeeData =
            new EmployeeData();
    private EmployeeTimes employeeTimes
            = new EmployeeTimes();

    /**
     * Create an object with the current context
     *
     * @param context the context to be tied to this helper
     */
    public EmployeeDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(employeeData.getSqlCreateEntries());
            db.execSQL(employeeTimes.getSqlCreateEntries());
        } catch (SQLiteException e) {
            Log.e("SQLITEEXCEP", "SQLiteException thrown: " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(employeeData.getSqlDeleteEntries());
        db.execSQL(employeeTimes.getSqlDeleteEntries());
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
