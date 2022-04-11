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
        String modulePath = getResources().getString(R.string.MODULE_PATH);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        final Response[] response = new Response[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = UserUtils.queryAPI(authority + studentPath + modulePath, params);
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

                                Log.i("STUDENT MODULE", data.toString());

                                RecyclerView recyclerView = view.findViewById(R.id.student_module_fragment_recycler_view);
                                ModuleAdapter moduleAdapter = new ModuleAdapter(view.getContext(), data);

                                recyclerView.setAdapter(moduleAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        ModuleAdapter.ArrayViewHolder arrayViewHolder = (ModuleAdapter.ArrayViewHolder) viewHolder;
                                        int position = arrayViewHolder.getAdapterPosition();

                                        final String PREF_FILE = "main_shared_preferences";
                                        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                        String auth_string = mPreferences.getString("auth_string", "");
                                        if (auth_string.equals("")) {
                                            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        JSONObject params = new JSONObject();
                                        try {
                                            params.put("auth_string", auth_string);
                                            params.put("module_id", moduleAdapter.dataSource.getJSONObject(position).get("module_id"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Log.i("STUDENT TOGGLE SCHEDULE", params.toString());
                                        String authority = getResources().getString(R.string.API_AUTHORITY);
                                        String studentPath = getResources().getString(R.string.STUDENT_PATH);
                                        String modulePath = getResources().getString(R.string.MODULE_PATH);
                                        String deletePath = getResources().getString(R.string.DELETE_PATH);

                                        ExecutorService executor = Executors.newSingleThreadExecutor();
                                        Looper uiLooper = Looper.getMainLooper();
                                        Handler handler = new Handler(uiLooper);
                                        final Response[] response = new Response[1];

                                        executor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    response[0] = UserUtils.queryAPI(authority + studentPath + modulePath + deletePath, params);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (response[0] == null) {
                                                            Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_SHORT).show();
                                                        } else if (response[0].code() == 200) {
                                                        } else {
                                                            Log.i("STUDENT DELETE MODULE", String.valueOf(response[0].code()));
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        moduleAdapter.dataSource.remove(position);
                                        reloadData(view);
                                    }
                                };

                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.i("STUDENT MODULE", String.valueOf(response[0].code()));
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

        Button addButton = view.findViewById(R.id.student_module_fragment_add_button);
        EditText addModuleID = view.findViewById(R.id.student_module_fragment_add_module);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addModuleID.getText().toString().equals("")) {
                    Toast.makeText(view.getContext(), R.string.module_id_error, Toast.LENGTH_SHORT).show();
                } else {
                    final String PREF_FILE = "main_shared_preferences";
                    SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                    String auth_string = mPreferences.getString("auth_string", "");
                    if (auth_string.equals("")) {
                        Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject params = new JSONObject();
                    try {
                        params.put("auth_string", auth_string);
                        params.put("module_id", addModuleID.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String authority = getResources().getString(R.string.API_AUTHORITY);
                    String studentPath = getResources().getString(R.string.STUDENT_PATH);
                    String modulePath = getResources().getString(R.string.MODULE_PATH);
                    String addPath = getResources().getString(R.string.ADD_PATH);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Looper uiLooper = Looper.getMainLooper();
                    Handler handler = new Handler(uiLooper);
                    final Response[] response = new Response[1];

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                response[0] = UserUtils.queryAPI(authority + studentPath + modulePath + addPath, params);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (response[0] == null) {
                                        Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_SHORT).show();
                                    } else if (response[0].code() == 200) {
                                        reloadData(getView());
                                        Toast.makeText(view.getContext(), R.string.successful_module_add, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(view.getContext(), R.string.unsuccessful_module_add, Toast.LENGTH_SHORT).show();
                                        Log.i("STUDENT ADD MODULE", String.valueOf(response[0].code()));
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}