package com.vince.capstoneproject.Interfaces;

/**
 * This is a functional interface designed to be implemented as a callback.
 * I needed a way to execute a block of code in a class outside of the AccessDatabaseTask class
 * The way to do it was to declare a variable of this type in the AccessDatabaseTask class, and
 * implement it inside of the class that I want to execute code in. In this case, the class that
 * implements this interface is {@link com.vince.capstoneproject.activities.CreateAccountActivity}.
 * Once the AccessDatabaseTask is finished, the code is triggered.
 */
@FunctionalInterface
public interface AsyncIfComplete {
    void onComplete();
}
