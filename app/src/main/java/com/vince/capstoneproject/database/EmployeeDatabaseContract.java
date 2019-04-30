package com.vince.capstoneproject.database;

import android.provider.BaseColumns;

/**
 * The purpose of this class, as outlined in the Android documentation, is to provide a blueprint
 * for our database and its tables. The main outer class represents the database, and each inner
 * class represents a table inside that database.
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class EmployeeDatabaseContract {
    public EmployeeDatabaseContract() {
    }

    public static class EmployeeData implements BaseColumns {

        /**
         * The name of the table in the employee.db database.
         */
        public static final String TABLE_NAME = "employee_data";

        /**
         * The primary key field "_id"
         */
        public static final String ID = BaseColumns._ID;

        /**
         * The column that holds the employee's first name
         */
        public static final String FIRST_NAME = "first_name";

        /**
         * The column that holds the employee's last name
         */
        public static final String LAST_NAME = "last_name";

        /**
         * The column that hold the employee's username
         */
        public static final String USERNAME = "username";

        /**
         * The column that holds the employee's password
         */
        public static final String PASSWORD = "password";

        /**
         * The SQL statement that creates a table using the other final Strings as the column names
         */
        private static final String SQL_CREATE_ENTRIES_EMPLOYEE_DATA =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        FIRST_NAME + " TEXT NOT NULL, " +
                        LAST_NAME + " TEXT NOT NULL, " +
                        USERNAME + " TEXT NOT NULL UNIQUE, " +
                        PASSWORD + " TEXT NOT NULL)";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * @return A string that contains the query to create the employee_data table and its columns
         */
        public String getSqlCreateEntries() {
            return SQL_CREATE_ENTRIES_EMPLOYEE_DATA;
        }

        /**
         * @return A string that contains the query to delete the employee_data table if it exists
         */
        public String getSqlDeleteEntries() {
            return SQL_DELETE_ENTRIES;
        }
    }

    /**
     * Provides a blueprint for a table in our database
     */
    public static class EmployeeTimes implements BaseColumns {

        /**
         * The name of the table in the database
         */
        public static final String TABLE_NAME = "employee_times";

        /**
         * The primary key field in the database
         */
        public static final String ID = BaseColumns._ID;

        /**
         * The column in the database to hold the clock in time
         */
        public static final String CLOCK_IN_TIME = "clock_in";

        /**
         * The column in the database to hold the clock out time
         */
        public static final String CLOCK_OUT_TIME = "clock_out";

        /**
         * The column in the database to hold the foreign key linked to the {@link EmployeeData}
         * table
         */
        public static final String EMPLOYEE_DATA_ID = "employee_data_id";

        public static final String NOTES = "notes";

        /**
         * The SQL statement that creates a table using the other final Strings as the column names
         */
        private static final String SQL_CREATE_ENTRIES_EMPLOYEE_TIMES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        ID + " INTEGER PRIMARY KEY, " +
                        CLOCK_IN_TIME + " TEXT NOT NULL, " +
                        CLOCK_OUT_TIME + " TEXT NOT NULL, " +
                        NOTES + " TEXT, " +
                        EMPLOYEE_DATA_ID + " INTEGER, " +
                        "FOREIGN KEY(employee_data_id) REFERENCES employee_data(id))";

        /**
         * Drops this table if it exists in the database
         */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Gets the SQL Create statement for this table
         *
         * @return the global field representing the SQL statement to create this table in the
         * database
         */
        public String getSqlCreateEntries() {
            return SQL_CREATE_ENTRIES_EMPLOYEE_TIMES;
        }

        /**
         * Gets the SQL Delete statement for this table
         *
         * @return the global field representing the SQL statement to create this table in the
         * database
         */
        public String getSqlDeleteEntries() {
            return SQL_DELETE_ENTRIES;
        }
    }
}
