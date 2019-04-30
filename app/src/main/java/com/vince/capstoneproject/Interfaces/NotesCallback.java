package com.vince.capstoneproject.Interfaces;

/**
 * Functional interface used to pass a type E from the AsyncTask into another class.
 *
 * @param <E> the type of the data to be passed.
 */
public interface NotesCallback<E> {
    void passNotes(E note);
}
