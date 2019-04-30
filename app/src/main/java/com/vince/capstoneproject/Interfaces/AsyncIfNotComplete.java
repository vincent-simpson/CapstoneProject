package com.vince.capstoneproject.Interfaces;

/**
 * This callback is used for when one of the AsyncTasks does not complete successfully.
 */
@FunctionalInterface
public interface AsyncIfNotComplete {
    void onFailure();
}
