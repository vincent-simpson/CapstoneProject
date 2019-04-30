package com.vince.capstoneproject.Interfaces;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * This is a functional Interface that is used to pass a two dimensional Array List from an Async
 * Task into another class.
 */
@FunctionalInterface
public interface AsyncResponse {
    void processFinish(ArrayList<ArrayList<LocalDateTime>> output);
}
