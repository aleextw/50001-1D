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

import androidx.fragment.app.Fragment;

import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;


public class ProfileFragment extends Fragment {

    public ProfileFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

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