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

public class ProfessorScheduleFragment extends Fragment {
    final int REQUEST_CODE_NEW_SCHEDULE_ITEM = 1000;

    public ProfessorScheduleFragment(){}

    void reloadData(View view) {
        // reloadData() used to refresh fragment outside of onCreate()
        // Get auth_string
        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        String auth_string = mPreferences.getString("auth_string", "");
        if (auth_string.equals("")) {
            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create new JSON object with auth_string
        JSONObject params = new JSONObject();
        try {
            params.put("auth_string", auth_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Setup variables for API endpoint
        String authority = getResources().getString(R.string.API_AUTHORITY);
        String professorPath = getResources().getString(R.string.FACULTY_PATH);
        String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);

        // Query API in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        final Response[] response = new Response[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = UserUtils.queryAPI(authority + professorPath + schedulePath, params);
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
                            // Success; get data from response as JSON array
                            String result = null;
                            try {
                                result = response[0].body().string();
                                JSONArray data = new JSONArray(result);

                                Log.i("PROFESSOR SCHEDULE", data.toString());

                                // Create new ScheduleAdapter and attach to RecyclerView
                                RecyclerView recyclerView = view.findViewById(R.id.professor_schedule_fragment_recycler_view);
                                ProfessorScheduleAdapter professorScheduleAdapter = new ProfessorScheduleAdapter(view.getContext(), data);

                                recyclerView.setAdapter(professorScheduleAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

                                // Create ItemTouchHelper to enable swiping to delete schedule items
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        ProfessorScheduleAdapter.ArrayViewHolder arrayViewHolder = (ProfessorScheduleAdapter.ArrayViewHolder) viewHolder;
                                        int position = arrayViewHolder.getAdapterPosition();

                                        try {
                                            // Show AlertDialog to confirm deletion of schedule item
                                            new AlertDialog.Builder(view.getContext())
                                                    .setTitle("Confirm Deletion")
                                                    .setMessage("Do you really want to delete schedule item '" + professorScheduleAdapter.dataSource.getJSONObject(position).get("description") + "'?")
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                            if (whichButton == DialogInterface.BUTTON_POSITIVE) {
                                                                // Confirmed
                                                                // Get auth_string
                                                                final String PREF_FILE = "main_shared_preferences";
                                                                SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                                                String auth_string = mPreferences.getString("auth_string", "");
                                                                if (auth_string.equals("")) {
                                                                    Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
                                                                    return;
                                                                }

                                                                // Create JSON object with auth_string and id of schedule item to delete
                                                                JSONObject params = new JSONObject();
                                                                try {
                                                                    params.put("auth_string", auth_string);
                                                                    params.put("id", professorScheduleAdapter.dataSource.getJSONObject(position).get("id"));
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                                Log.i("PROFESSOR DEL SCHEDULE", params.toString());

                                                                // Setup variables for API endpoint
                                                                String authority = getResources().getString(R.string.API_AUTHORITY);
                                                                String professorPath = getResources().getString(R.string.FACULTY_PATH);
                                                                String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);
                                                                String deletePath = getResources().getString(R.string.DELETE_PATH);

                                                                // Query API in background thread
                                                                ExecutorService executor = Executors.newSingleThreadExecutor();
                                                                Looper uiLooper = Looper.getMainLooper();
                                                                Handler handler = new Handler(uiLooper);
                                                                final Response[] response = new Response[1];

                                                                executor.execute(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            response[0] = UserUtils.queryAPI(authority + professorPath + schedulePath + deletePath, params);
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
                                                                                    // Success; show success Toast
                                                                                    Toast.makeText(view.getContext(), R.string.successful_schedule_delete, Toast.LENGTH_SHORT).show();
                                                                                } else {
                                                                                    // API error; show generic error Toast and log response code
                                                                                    Toast.makeText(view.getContext(), R.string.unsuccessful_schedule_delete, Toast.LENGTH_SHORT).show();
                                                                                    Log.i("PROFESSOR DEL SCHEDULE", String.valueOf(response[0].code()));
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                // Remove schedule item and refresh fragment
                                                                professorScheduleAdapter.dataSource.remove(position);
                                                                professorScheduleAdapter.notifyDataSetChanged();
                                                            }
                                                        }})
                                                    .setNegativeButton(android.R.string.no, null).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                // Attach touch helper to RecyclerView
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.i("PROFESSOR SCHEDULE", String.valueOf(response[0].code()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.professor_fragment_schedule, container, false);
        reloadData(view);

        // Enable swiping down to refresh fragment
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.professor_schedule_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                reloadData(view);
            }
        });

        TextView addButton = view.findViewById(R.id.professor_schedule_fragment_add_button);

        // Handle add-schedule-item button press
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Explicit intent to ProfessorAddScheduleItemActivity
                Intent intent = new Intent(getView().getContext(), ProfessorAddScheduleItemActivity.class);
                startActivityForResult(intent, REQUEST_CODE_NEW_SCHEDULE_ITEM);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If return from activity is ok, means schedule item added successfully
        // Refresh fragment
        if (resultCode == Activity.RESULT_OK) {
            reloadData(getView());
        }
    }
}