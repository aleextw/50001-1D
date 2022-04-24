package com.example.infosys1d;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ProfileFragment extends Fragment {

    public ProfileFragment(){
        // require a empty public constructor
    }
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
        String usersPath = getResources().getString(R.string.USERS_PATH);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        final Response[] response = new Response[1];

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = UserUtils.queryAPI(authority + studentPath + usersPath, params);
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

                                TextView firstNameView = view.findViewById(R.id.profile_fragment_firstname);
                                TextView lastNameView = view.findViewById(R.id.profile_fragment_lastname);
                                TextView studentIDView = view.findViewById(R.id.profile_fragment_studentID);
//                                ModuleAdapter moduleAdapter = new ModuleAdapter(view.getContext(), data);


                                firstNameView.setText(data.getJSONObject(0).getString("first_name"));
                                lastNameView.setText(data.getJSONObject(0).getString("last_name"));
                                studentIDView.setText(data.getJSONObject(0).getString("username"));


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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        reloadData(view);//TODO

        TextView logoutButton = view.findViewById(R.id.profile_fragment_signout);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String authority = getResources().getString(R.string.API_AUTHORITY);
                String logout = getResources().getString(R.string.LOGOUT_PATH);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Looper uiLooper = Looper.getMainLooper();
                Handler handler = new Handler(uiLooper);
                final Response[] response = new Response[1];

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            response[0] = UserUtils.queryAPI(authority + logout, params);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (response[0] == null) {
                                    Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_SHORT).show();
                                } else if (response[0].code() == 200) {
                                    Toast.makeText(view.getContext(), R.string.successful_logout, Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(view.getContext(), LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(view.getContext(), R.string.unsuccessful_logout, Toast.LENGTH_SHORT).show();
                                    Log.i("LOGOUT", String.valueOf(response[0].code()));
                                }
                            }
                        });
                    }
                });
            }
        });

        return view;
    }
}