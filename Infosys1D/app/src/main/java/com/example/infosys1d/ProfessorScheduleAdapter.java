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

public class ProfessorScheduleAdapter extends RecyclerView.Adapter<ProfessorScheduleAdapter.ArrayViewHolder> {
    // This class is used as an adapter for a RecyclerView to display a faculty member's schedule items
    // Code adapted from 50001 lessons

    Context context;
    LayoutInflater mInflater;
    JSONArray dataSource;

    ProfessorScheduleAdapter(Context context, JSONArray arr) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.dataSource = arr;
    }

    static class ArrayViewHolder extends RecyclerView.ViewHolder{
        TextView moduleName, deadlineDate, deadlineDescription, deadlineTally;
        ArrayViewHolder(View view){
            super(view);
            moduleName = view.findViewById(R.id.professor_schedule_module);
            deadlineDate = view.findViewById(R.id.professor_schedule_date);
            deadlineDescription = view.findViewById(R.id.professor_schedule_description);
            deadlineTally = view.findViewById(R.id.professor_schedule_tally);
        }
    }

    @NonNull
    @Override
    public ProfessorScheduleAdapter.ArrayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.professor_schedule, viewGroup, false);
        return new ProfessorScheduleAdapter.ArrayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfessorScheduleAdapter.ArrayViewHolder arrayViewHolder, int i) {
        try {
            arrayViewHolder.moduleName.setText(dataSource.getJSONObject(i).getString("module_name"));
            arrayViewHolder.deadlineDate.setText(dataSource.getJSONObject(i).getString("date"));
            arrayViewHolder.deadlineDescription.setText(dataSource.getJSONObject(i).getString("description"));
            arrayViewHolder.deadlineTally.setText(dataSource.getJSONObject(i).getString("tally"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataSource.length();
    }
}
