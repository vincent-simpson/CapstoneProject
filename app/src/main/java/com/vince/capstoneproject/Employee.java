package com.vince.capstoneproject;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this class is to hold the data retrieved from running queries on our database.
 * By placing the data inside this object, we can then create a List of such objects and pass them
 * back as a result set.
 */
public class Employee implements Serializable {
    public static String username = "";
    private int id;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private long diffInMilliseconds;
    private String notes;

    public Employee() {
    }

    public Employee(int idNew, String firstNameNew, String lastNameNew, String passwordNew) {
        this.id = idNew;
        this.firstName = firstNameNew;
        this.lastName = lastNameNew;
        this.password = passwordNew;
    }

    public Employee(String firstNameNew, String lastNameNew, String passwordNew) {
        this.firstName = firstNameNew;
        this.lastName = lastNameNew;
        this.password = passwordNew;
    }

    /**
     * Gets the user's id
     *
     * @return the id of the user
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of the user
     *
     * @param id the id of the user
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the first name of the user
     *
     * @return the first name of the user as a String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name to set to the user.
     */
    @SuppressWarnings("unused")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the last name of the user.
     *
     * @return the last name of the user as a String
     */
    public String getLastName() {
        return lastName;
    }

    @SuppressWarnings("unused")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password of the user.
     *
     * @return the password of the user as a String
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user
     *
     * @param password the password to set to the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the clock in time of the user.
     *
     * @return the clock in time of the user.
     */
    public LocalDateTime getClockInTime() {
        return clockInTime;
    }

    /**
     * Sets the clock in time of the user
     *
     * @param clockInTime the time to set as the clock in time for the user
     */
    public void setClockInTime(LocalDateTime clockInTime) {
        this.clockInTime = clockInTime;
    }

    /**
     * Gets the clock out time for the user
     *
     * @return the clock out time for the user
     */
    public LocalDateTime getClockOutTime() {
        return clockOutTime;
    }

    /**
     * Sets the clock out time of the user
     *
     * @param clockOutTime the clock out time to set to the user.
     */
    public void setClockOutTime(LocalDateTime clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    /**
     * Gets the difference of the clock in and clock out times in milliseconds
     *
     * @return a long representing the difference of clock in and clock out times
     */
    public long getDiffInMilliseconds() {
        return diffInMilliseconds;
    }

    @SuppressWarnings("unused")
    public void setDiffInMilliseconds(long diffInMilliseconds) {
        this.diffInMilliseconds = diffInMilliseconds;
    }

    /**
     * Gets the notes assigned to this user
     *
     * @return the notes as a String
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the notes of the user
     *
     * @param notes the notes to set to the user.
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Converts this user to a String. The only information that is needed is the total time clocked
     * in.
     *
     * @return a string representing the total time clocked in.
     */
    @NonNull
    @Override
    public String toString() {
        return "Total time clocked in: \n" + String.format(Locale.ENGLISH,
                "%d hours, %d min, %d sec",
                TimeUnit.MILLISECONDS.toHours(getDiffInMilliseconds()),
                TimeUnit.MILLISECONDS.toMinutes(getDiffInMilliseconds()),
                TimeUnit.MILLISECONDS.toSeconds(getDiffInMilliseconds()));
    }
}
