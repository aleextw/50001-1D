package com.example.infosys1d;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

public class StudentScheduleAdapter extends RecyclerView.Adapter<StudentScheduleAdapter.ArrayViewHolder> {
    // This class is used as an adapter for a RecyclerView to display a student's schedule items
    // Code adapted from 50001 lessons

    Context context;
    LayoutInflater mInflater;
    JSONArray dataSource;

    StudentScheduleAdapter(Context context, JSONArray arr) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.dataSource = arr;
    }

    static class ArrayViewHolder extends RecyclerView.ViewHolder{
        TextView moduleName, deadlineDate, deadlineDescription, deadlineDone;
        ArrayViewHolder(View view){
            super(view);
            moduleName = view.findViewById(R.id.student_schedule_module);
            deadlineDate = view.findViewById(R.id.student_schedule_date);
            deadlineDescription = view.findViewById(R.id.student_schedule_description);
            deadlineDone = view.findViewById(R.id.student_schedule_done);
        }

    }

    @NonNull
    @Override
    public StudentScheduleAdapter.ArrayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.student_schedule, viewGroup, false);
        return new StudentScheduleAdapter.ArrayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentScheduleAdapter.ArrayViewHolder arrayViewHolder, int i) {
        try {
            arrayViewHolder.moduleName.setText(dataSource.getJSONObject(i).getString("module_name"));
            arrayViewHolder.deadlineDate.setText(dataSource.getJSONObject(i).getString("date"));
            arrayViewHolder.deadlineDescription.setText(dataSource.getJSONObject(i).getString("description"));
            arrayViewHolder.deadlineDone.setText(dataSource.getJSONObject(i).getString("done"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.length();
    }
}
