package com.example.infosys1d;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;


public class StudentScheduleFragment extends Fragment {

    public StudentScheduleFragment(){
        // require a empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final JSONObject[] data = new JSONObject[1];

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Looper uiLooper = Looper.getMainLooper();
        Handler handler = new Handler(uiLooper);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    data[0] = DatabaseUtils.getStudentData(container.getContext());
                    Log.i("DATABASE", data[0].toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Iterator keys = data[0].keys();

                        while (keys.hasNext()) {
                            Object key = keys.next();
                            JSONObject value = null;
                            try {
                                value = data[0].getJSONObject((String) key);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                String component = value.getString("");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

//        RecyclerView recyclerView = container.findViewById(R.id.module_recycler_view);
//        ModuleAdapter moduleAdapter = new ModuleAdapter(container.getContext(), data);
//        recyclerView.setAdapter(moduleAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.student_fragment_schedule, container, false);
    }
}

