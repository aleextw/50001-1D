package com.example.infosys1d;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentModuleFragment extends Fragment {
    public StudentModuleFragment(){}

    void reloadData(View view) {
        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        String auth_string = mPreferences.getString("auth_string", "");
        if (auth_string.equals("")) {
            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
            return;
        }

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
                            Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_LONG).show();
                        } else if (response[0].code() == 200) {
                            String result = null;
                            try {
                                result = response[0].body().string();
                                JSONArray data = new JSONArray(result);

                                Log.i("STUDENT SCHEDULE", data.toString());

                                RecyclerView recyclerView = view.findViewById(R.id.student_schedule_fragment_recycler_view);
                                StudentScheduleAdapter studentScheduleAdapter = new StudentScheduleAdapter(view.getContext(), data);

                                recyclerView.setAdapter(studentScheduleAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        StudentScheduleAdapter.ArrayViewHolder arrayViewHolder = (StudentScheduleAdapter.ArrayViewHolder) viewHolder;
                                        int position = arrayViewHolder.getAdapterPosition();

                                        final String PREF_FILE = "main_shared_preferences";
                                        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                        String auth_string = mPreferences.getString("auth_string", "");
                                        if (auth_string.equals("")) {
                                            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        JSONObject params = new JSONObject();
                                        try {
                                            params.put("auth_string", auth_string);
                                            params.put("module_id", studentScheduleAdapter.dataSource.getJSONObject(position).get("module_id"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.i("STUDENT TOGGLE SCHEDULE", params.toString());
                                        String authority = getResources().getString(R.string.API_AUTHORITY);
                                        String studentPath = getResources().getString(R.string.STUDENT_PATH);
                                        String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);
                                        String togglePath = getResources().getString(R.string.TOGGLE_PATH);

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
                                                            Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_LONG).show();
                                                        } else if (response[0].code() == 200) {
                                                        } else {
                                                            Log.i("STUDENT TOGGLE SCHEDULE", String.valueOf(response[0].code()));
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        studentScheduleAdapter.dataSource.remove(position);
                                        studentScheduleAdapter.notifyDataSetChanged();
                                    }
                                };

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
        View view = inflater.inflate(R.layout.student_fragment_modules, container, false);
        reloadData(view);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.student_module_fragment_swipe_refresh_layout);
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