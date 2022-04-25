package com.example.infosys1d;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentScheduleFragment extends Fragment {
    public StudentScheduleFragment(){}

    void reloadData(View view) {
        // reloadData used to refresh the schedule fragment
        // Get auth_string from Shared Preferences
        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        String auth_string = mPreferences.getString("auth_string", "");
        if (auth_string.equals("")) {
            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create new JSON object and query our API for our schedule data in background thread
        JSONObject params = new JSONObject();
        try {
            params.put("auth_string", auth_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String authority = getResources().getString(R.string.API_AUTHORITY);
        String studentPath = getResources().getString(R.string.STUDENT_PATH);
        String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        final Response[] response = new Response[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = UserUtils.queryAPI(authority + studentPath + schedulePath, params);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response[0] == null) {
                            // If no response, API might be down; show generic error Toast to user
                            Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_LONG).show();
                        } else if (response[0].code() == 200) {
                            // Success
                            String result = null;
                            try {
                                // Parse our response data as a JSON array and pass to our Adapter
                                result = response[0].body().string();
                                JSONArray data = new JSONArray(result);

                                Log.i("STUDENT SCHEDULE", data.toString());

                                RecyclerView recyclerView = view.findViewById(R.id.student_schedule_fragment_recycler_view);
                                StudentScheduleAdapter studentScheduleAdapter = new StudentScheduleAdapter(view.getContext(), data);

                                // Recycler view setup
                                recyclerView.setAdapter(studentScheduleAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

                                // Add ItemTouchHelper to handle swiping of items
                                // Swipe to mark schedule item as done / completed
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        StudentScheduleAdapter.ArrayViewHolder arrayViewHolder = (StudentScheduleAdapter.ArrayViewHolder) viewHolder;
                                        int position = arrayViewHolder.getAdapterPosition();

                                        // Get auth_string from shared preferences
                                        final String PREF_FILE = "main_shared_preferences";
                                        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                        String auth_string = mPreferences.getString("auth_string", "");
                                        if (auth_string.equals("")) {
                                            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        // Create JSON object with auth_string and id of item to mark as complete
                                        JSONObject params = new JSONObject();
                                        try {
                                            params.put("auth_string", auth_string);
                                            params.put("id", studentScheduleAdapter.dataSource.getJSONObject(position).get("id"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Log.i("STUDENT TOGGLE SCHED", params.toString());

                                        // Setup variables for API endpoint
                                        String authority = getResources().getString(R.string.API_AUTHORITY);
                                        String studentPath = getResources().getString(R.string.STUDENT_PATH);
                                        String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);
                                        String togglePath = getResources().getString(R.string.TOGGLE_PATH);

                                        // Query API in background thread
                                        ExecutorService executor = Executors.newSingleThreadExecutor();
                                        Looper uiLooper = Looper.getMainLooper();
                                        Handler handler = new Handler(uiLooper);
                                        final Response[] response = new Response[1];

                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    response[0] = UserUtils.queryAPI(authority + studentPath + schedulePath + togglePath, params);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (response[0] == null) {
                                                            // If no response, API might be down; show generic error Toast to user
                                                            Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_SHORT).show();
                                                        } else if (response[0].code() == 200) {
                                                            // Success, show success Toast to user
                                                            Toast.makeText(view.getContext(), R.string.successful_schedule_toggle, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // API error, show generic error Toast to user and log the response code
                                                            Toast.makeText(view.getContext(), R.string.unsuccessful_schedule_toggle, Toast.LENGTH_SHORT).show();
                                                            Log.i("STUDENT TOGGLE SCHED", String.valueOf(response[0].code()));
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                        // Reload the fragment whether successful or not
                                        reloadData(view);
                                    }
                                };

                                // Attach our touch helper to our RecyclerView
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.i("STUDENT SCHEDULE", String.valueOf(response[0].code()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.student_fragment_schedule, container, false);
        reloadData(view);

        // Enable swiping down to refresh our fragment
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.student_schedule_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                reloadData(view);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}