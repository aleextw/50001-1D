package com.example.infosys1d;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class ProfessorModuleFragment extends Fragment {
    public ProfessorModuleFragment(){}

    void reloadData(View view) {
        // reloadData() used to refresh our fragment outside of onCreate()
        // Get auth_string from Shared Preferences
        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        String auth_string = mPreferences.getString("auth_string", "");
        if (auth_string.equals("")) {
            Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a new JSON object with auth_string
        JSONObject params = new JSONObject();
        try {
            params.put("auth_string", auth_string);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Setup variables for API endpoint
        String authority = getResources().getString(R.string.API_AUTHORITY);
        String professorPath = getResources().getString(R.string.FACULTY_PATH);
        String modulePath = getResources().getString(R.string.MODULE_PATH);

        // Query API in background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        final Response[] response = new Response[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = UserUtils.queryAPI(authority + professorPath + modulePath, params);
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
                            // Success; get data from response body as JSON array
                            String result = null;
                            try {
                                result = response[0].body().string();
                                JSONArray data = new JSONArray(result);

                                Log.i("PROFESSOR MODULES", data.toString());

                                // Create new module adapter and attach to RecyclerView
                                RecyclerView recyclerView = view.findViewById(R.id.professor_module_fragment_recycler_view);
                                ModuleAdapter moduleAdapter = new ModuleAdapter(view.getContext(), data);

                                recyclerView.setAdapter(moduleAdapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

                                // Create ItemTouchHelper to enable swiping to delete modules
                                ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        ModuleAdapter.ArrayViewHolder arrayViewHolder = (ModuleAdapter.ArrayViewHolder) viewHolder;
                                        int position = arrayViewHolder.getAdapterPosition();

                                        try {
                                            // Show AlertDialog to double-confirm that they want to delete the module
                                            new AlertDialog.Builder(view.getContext())
                                                    .setTitle("Confirm Deletion")
                                                    .setMessage("Do you really want to delete module " + moduleAdapter.dataSource.getJSONObject(position).get("module_id") + "?")
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

                                                                // Create new JSON object with auth_string and ID of module to delete
                                                                JSONObject params = new JSONObject();
                                                                try {
                                                                    params.put("auth_string", auth_string);
                                                                    params.put("module_id", moduleAdapter.dataSource.getJSONObject(position).get("module_id"));
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                                Log.i("PROFESSOR DELETE MODULE", params.toString());

                                                                // Setup variables for API endpoint
                                                                String authority = getResources().getString(R.string.API_AUTHORITY);
                                                                String professorPath = getResources().getString(R.string.FACULTY_PATH);
                                                                String modulePath = getResources().getString(R.string.MODULE_PATH);
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
                                                                            response[0] = UserUtils.queryAPI(authority + professorPath + modulePath + deletePath, params);
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
                                                                                    // Success; show success Toast
                                                                                    Toast.makeText(view.getContext(), R.string.successful_module_delete, Toast.LENGTH_LONG).show();
                                                                                } else {
                                                                                    // API error; show error Toast and log response code
                                                                                    Toast.makeText(view.getContext(), R.string.unsuccessful_module_delete, Toast.LENGTH_LONG).show();
                                                                                    Log.i("PROFESSOR DELETE MODULE", String.valueOf(response[0].code()));
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                                // Remove module and update fragment
                                                                moduleAdapter.dataSource.remove(position);
                                                                moduleAdapter.notifyDataSetChanged();
                                                            }
                                                        }})
                                                    .setNegativeButton(android.R.string.no, null).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                // Attach our touch helper to our RecyclerView
                                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);

                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Log.i("PROFESSOR MODULES", String.valueOf(response[0].code()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.professor_fragment_modules, container, false);
        reloadData(view);

        // Enable swiping down to refresh fragment
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.professor_module_fragment_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                reloadData(view);
            }
        });

        Button addButton = view.findViewById(R.id.professor_module_fragment_add_button);
        EditText addModuleID = view.findViewById(R.id.professor_module_fragment_add_module);
        EditText addModuleName = view.findViewById(R.id.professor_module_fragment_add_module_name);

        // Handle button press to add new module
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if any fields are empty and show error Toast
                if (addModuleID.getText().toString().equals("")) {
                    Toast.makeText(view.getContext(), R.string.module_id_error, Toast.LENGTH_SHORT).show();
                } else if (addModuleName.getText().toString().equals("")) {
                    Toast.makeText(view.getContext(), R.string.module_name_error, Toast.LENGTH_SHORT).show();
                } else {
                    // Get auth_string
                    final String PREF_FILE = "main_shared_preferences";
                    SharedPreferences mPreferences = view.getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                    String auth_string = mPreferences.getString("auth_string", "");
                    if (auth_string.equals("")) {
                        Toast.makeText(view.getContext(), "Invalid auth string.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create new JSON object with auth_string and module details
                    JSONObject params = new JSONObject();
                    try {
                        params.put("auth_string", auth_string);
                        params.put("module_id", addModuleID.getText().toString());
                        params.put("module_name", addModuleName.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Setup variables for API endpoint
                    String authority = getResources().getString(R.string.API_AUTHORITY);
                    String professorPath = getResources().getString(R.string.FACULTY_PATH);
                    String modulePath = getResources().getString(R.string.MODULE_PATH);
                    String addPath = getResources().getString(R.string.ADD_PATH);

                    // Query API in background thread
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Looper uiLooper = Looper.getMainLooper();
                    Handler handler = new Handler(uiLooper);
                    final Response[] response = new Response[1];

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                response[0] = UserUtils.queryAPI(authority + professorPath + modulePath + addPath, params);
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
                                        // Success; show success Toast and refresh fragment
                                        reloadData(getView());
                                        Toast.makeText(view.getContext(), R.string.successful_module_add, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // API error; show error Toast and log response code
                                        Toast.makeText(view.getContext(), R.string.unsuccessful_module_add, Toast.LENGTH_SHORT).show();
                                        Log.i("PROFESSOR ADD MODULE", String.valueOf(response[0].code()));
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