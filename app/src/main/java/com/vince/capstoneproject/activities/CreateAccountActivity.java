package com.vince.capstoneproject.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.Interfaces.AsyncIfComplete;
import com.vince.capstoneproject.Interfaces.AsyncIfNotComplete;
import com.vince.capstoneproject.R;
import com.vince.capstoneproject.database.AccessDatabaseTask;
import com.vince.capstoneproject.database.AccessDatabaseTask.Operation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for letting the user create a new username and password. It then
 * runs an AsyncTask to insert the data into the database.
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class CreateAccountActivity extends AppCompatActivity
        implements
        AsyncIfComplete,
        AsyncIfNotComplete {

    private boolean passwordIsValid;
    private boolean usernameIsValid;
    private boolean nameIsValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        EditText firstName = findViewById(R.id.create_account_first_name),
                lastName = findViewById(R.id.create_account_last_name),
                username = findViewById(R.id.create_account_user_name),
                password = findViewById(R.id.create_account_password),
                password_reentry = findViewById(R.id.create_account_password2);
        Button cancel = findViewById(R.id.cancel_create_account_button),
                submit = findViewById(R.id.create_account_submit);

        submit.setOnClickListener(e -> {
            /*
            What we need to do in this event listener consists of three things.
            1: we need to validate that the two passwords are the same, as well as it
            contains at least one uppercase letter and one special character.
            2: We need to make sure that the username fits our specified criteria.
            3: We need to validate the first name, last name fields.
             */
            String username_str = username.getText().toString();
            String password1 = password.getText().toString();
            String password2 = password_reentry.getText().toString();
            String firstName_str = firstName.getText().toString();
            String lastName_str = lastName.getText().toString();

            /*
            This validates the two password fields. If they're not the same, then we clear both
            fields and change the hint text to red, and we also display an error message.
             */
            if (validPassword(password1, password2)) {
                passwordIsValid = true;
            } else {
                password.setHint("Invalid password!");
                password.setHintTextColor(Color.RED);
                password_reentry.getText().clear();
                password.getText().clear();
            }

            if (validUsername(username_str)) {
                usernameIsValid = true;
            } else {
                username.setHint("Invalid username!");
                username.setHintTextColor(Color.RED);
                username.getText().clear();
            }

            if (validName(firstName_str, lastName_str)) {
                nameIsValid = true;
            } else {
                firstName.setHint("Invalid name!");
                firstName.setHintTextColor(Color.RED);
                firstName.getText().clear();
                lastName.getText().clear();
            }

            //If the password, username, and first and last names are valid, we want to insert
            //them into the database
            if (passwordIsValid && usernameIsValid && nameIsValid) {
                Employee employee = new Employee(firstName_str, lastName_str, password1);
                Employee.username = username_str;
                AccessDatabaseTask databaseTask = new AccessDatabaseTask(employee,
                        Operation.INSERT);
                databaseTask.execute(getApplicationContext());
                databaseTask.completionCode = this;
                databaseTask.failureCode = this;
            }
        });

        cancel.setOnClickListener(e -> finish());
    }

    /**
     * Validates the entered username based on the following criteria:
     * Must be between 8 and 16 characters.
     *
     * @param s The entered information in the username TextField
     * @return True if the username length is at least 8 characters and less than or
     * equal to 16 characters.
     */
    private boolean validUsername(String s) {
        byte len = (byte) s.length();
        return len >= 8 && len <= 16;
    }

    /**
     * Validates the entered password based on the following criteria:
     * Password must be between 8 and 16 characters and contain at least one uppercase
     * letter and one special character. The passwords must also match.
     *
     * @param password1 The text from the first password TextField
     * @param password2 The text from the second (validation) password TextField
     * @return True if all of the above criteria is met.
     */
    public boolean validPassword(String password1, String password2) {
        Pattern pSpecialChars = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
        Pattern pCapitals = Pattern.compile("[A-Z]");
        Matcher matcher = pSpecialChars.matcher(password1);
        Matcher matcher2 = pCapitals.matcher(password1);

        int len = password1.length();
        boolean goodLength = len >= 8 && len <= 16;

        boolean goodMatch = password1.equals(password2);
        boolean goodChars = matcher.find() && matcher2.find();

        return goodChars && goodLength && goodMatch;
    }

    /**
     * Checks to make sure that the only characters entered in the name field were alphabetical
     *
     * @param firstName The text from the firstName TextField
     * @param lastName  The text from the lastName TextField
     * @return True if both fields contain only alphabetical characters.
     */
    private boolean validName(String firstName, String lastName) {
        Pattern pattern = Pattern.compile("[^A-Za-z0-9]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(firstName);
        boolean validFirstName, validLastName;

        validFirstName = !matcher.find();
        matcher = pattern.matcher(lastName);
        validLastName = !matcher.find();

        return validFirstName && validLastName;
    }

    /**
     * Called by the focus listener to hide the keyboard when the user taps outside the current
     * field.
     *
     * @param view The current view.
     */
    @SuppressWarnings("unused")
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                CreateAccountActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Called if the insert statement in
     * {@link AccessDatabaseTask#runInsertEmployeeStatement(Employee)}is successful
     */
    @Override
    public void onComplete() {
        Toast.makeText(getBaseContext(), "Account successfully created",
                Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Called if the username that is entered already exists in the database.
     */
    @Override
    public void onFailure() {
        Toast.makeText(getBaseContext(), "Insert failed. Duplicate username found",
                Toast.LENGTH_LONG).show();
    }
}


