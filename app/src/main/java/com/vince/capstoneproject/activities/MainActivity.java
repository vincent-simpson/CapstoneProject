package com.vince.capstoneproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.vince.capstoneproject.Employee;
import com.vince.capstoneproject.R;
import com.vince.capstoneproject.database.AccessDatabaseTask;
import com.vince.capstoneproject.database.LoginTask;

/**
 * The main activity of the application. This is the first activity the user sees when starting
 * the application.
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    private EditText usernameET, passwordET;
    private LoginTask loginTask;
    private Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);

        adminButton = findViewById(R.id.adminButton);

        usernameET.setOnFocusChangeListener(this);
        passwordET.setOnFocusChangeListener(this);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            login();
        });

        Button createAccountButton = findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(v -> {
            Intent i = new Intent(this, CreateAccountActivity.class);
            startActivity(i);
        });

        adminButton.setOnClickListener( v -> {

            AccessDatabaseTask insertAdmin = new AccessDatabaseTask(
                    AccessDatabaseTask.Operation.ADMIN);

            insertAdmin.execute(getApplicationContext());
        });
    }

    /**
     * Gets the data entered in the username and password TextViews. Creates a new
     * {@link Employee} object with the username and password. Sets the static
     * {@link Employee#username} field with the text from the username TextView.
     * Starts a {@link LoginTask} to try to log the user in.
     */
    public void login() {
        String usernameInput = usernameET.getText().toString().trim();
        String passwordInput = passwordET.getText().toString().trim();

        Employee employee = new Employee();
        Employee.username = usernameInput;
        employee.setPassword(passwordInput);

        loginTask = new LoginTask(employee, this);
        loginTask.execute(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loginTask != null) {
            loginTask.dialog.cancel();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) hideKeyboard(v);
    }

    /**
     * Called by the focus listener to hide the keyboard when the user taps outside the current
     * field.
     *
     * @param view The current view.
     */
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                CreateAccountActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
