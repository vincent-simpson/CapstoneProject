package com.vince.capstoneproject;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vince.capstoneproject.Interfaces.NotesCallback;
import com.vince.capstoneproject.activities.AddJobActivity;
import com.vince.capstoneproject.activities.EmployeeDashboardActivity;
import com.vince.capstoneproject.database.AccessDatabaseTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This adapter takes in a data-set and draws views onto a RecyclerView widget.
 *
 *
 * Pieces of this code have been referenced from the official Android SDK documentation at
 * https://developer.android.com/docs
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<ArrayList<LocalDateTime>> data;
    private Context context;
    private Employee employee;
    private ArrayList<LocalDateTime> mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;

    public RecyclerViewAdapter(ArrayList<ArrayList<LocalDateTime>> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            NotesCallback<String>
    {
        private TextView mTitle;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.txtTitle);
            itemView.setOnClickListener(this);
        }

        /**
         * Called when a RecyclerView is clicked
         *
         * @param v the view that is clicked
         */
        @Override
        public void onClick(View v) {
            employee = new Employee();
            employee.setClockInTime(data.get(getAdapterPosition()).get(1));
            employee.setClockOutTime(data.get(getAdapterPosition()).get(0));
            System.out.println("EMPLOYEE CLOCK IN TIME: "  + employee.getClockInTime());

            AccessDatabaseTask getNotes = new AccessDatabaseTask(
                    AccessDatabaseTask.Operation.SELECT_NOTES,
                    employee.getClockInTime()
            );
            getNotes.notesCallback = this;
            getNotes.execute(context);

        }

        /**
         * Called after the AccessDatabaseTask in onClick is finished getting the notes.
         *
         * @param note the note to be passed into AddJobActivity.
         */
        @Override
        public void passNotes(String note) {
            System.out.println("Notes callback: " + note);
            Intent i = new Intent(context, AddJobActivity.class);
            if(note == null || note.equalsIgnoreCase("null")) {
                System.out.println("NOTES IS NULL");
                i.putExtra("notes", "");
            } else {
                System.out.println("NOTES IS NOT NULL");
                i.putExtra("notes", note);
            }
            i.putExtra("employee", employee);
            i.putExtra("callingClass", "RecyclerViewAdapter");
            context.startActivity(i);
        }
    }

    @Override
    @NonNull
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.cardview_row,
                parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (!data.isEmpty()) {
            //Temp holds each nested Array List
            ArrayList<LocalDateTime> temp = data.get(position);

            LocalDateTime clockOut = temp.get(0);
            LocalDateTime clockIn = temp.get(1);

            //Gets the total time clocked in.
            Duration duration = Duration.between(temp.get(1), temp.get(0));
            //Set the list item's text to a formatted version of the total time clocked in
            holder.mTitle.setText(returnFormatted(clockIn, clockOut, duration.toMillis()));

        }
    }

    /**
     * Takes the clock in time, the clock out time, and the difference between the two as parameters
     * and formats all of them into a string.
     *
     * @param clockInTime  the clock in time for each user
     * @param clockOutTime the clock out time for each user
     * @param time         the total duration which is the absolute value of the difference between the
     *                     two times.
     * @return a formatted String representing all of the above data.
     */
    private String returnFormatted(LocalDateTime clockInTime, LocalDateTime clockOutTime, long time) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");

        String datesFormatted = dateTimeFormatter.format(clockInTime) + " - " +
                dateTimeFormatter.format(clockOutTime);

        return "Total time clocked in: \n" +
                String.format(Locale.ENGLISH, "%d hours, %d min, %d seconds \n" +
                                "%s",
                        time / (60 * 60 * 1000),
                        time / (60 * 1000) % 60,
                        (time / 1000 % 60),
                        datesFormatted
                );
    }

    /**
     * Gets the size of the data-set.
     *
     * @return the size of {@link #data}
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Removes the item from the ViewAdapter
     *
     * @param position the position of the item to be removed from the ViewAdapter
     */
    public void removeItem(int position) {
            mRecentlyDeletedItem = this.data.get(position);
            mRecentlyDeletedItemPosition = position;
            notifyItemRemoved(position);
    }

    /**
     * Gets the data-set for this ViewAdapter.
     *
     * @return the data for this ViewAdapter.
     */
    public ArrayList<ArrayList<LocalDateTime>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<LocalDateTime>> data) {
        this.data = data;
    }

}


