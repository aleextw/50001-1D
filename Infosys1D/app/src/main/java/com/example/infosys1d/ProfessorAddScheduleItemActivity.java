package com.example.infosys1d;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfessorAddScheduleItemActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule_item);

        Button button = findViewById(R.id.add_schedule_item_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newItemModuleID = findViewById(R.id.add_schedule_item_id);
                EditText newItemDueDate = findViewById(R.id.add_schedule_item_date);
                EditText newItemDescription = findViewById(R.id.add_schedule_item_description);

                if (newItemModuleID.equals("") || newItemDueDate.equals("") || newItemDescription.equals("")) {
                    Toast.makeText(ProfessorAddScheduleItemActivity.this, R.string.new_schedule_item_empty_field, Toast.LENGTH_LONG).show();
                } else {
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
                        params.put("module_id", newItemModuleID.getText().toString());
                        params.put("date", newItemDueDate.getText().toString());
                        params.put("description", newItemDescription.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String authority = getResources().getString(R.string.API_AUTHORITY);
                    String professorPath = getResources().getString(R.string.FACULTY_PATH);
                    String schedulePath = getResources().getString(R.string.SCHEDULE_PATH);
                    String addPath = getResources().getString(R.string.ADD_PATH);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Looper uiLooper = Looper.getMainLooper();
                    Handler handler = new Handler(uiLooper);
                    final Response[] response = new Response[1];

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                response[0] = UserUtils.queryAPI(authority + professorPath + schedulePath + addPath, params);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (response[0] == null) {
                                        Toast.makeText(view.getContext(), R.string.database_error, Toast.LENGTH_LONG).show();
                                    } else if (response[0].code() == 200) {
                                        Toast.makeText(view.getContext(), R.string.successful_schedule_add, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent();
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();
                                    } else {
                                        Toast.makeText(view.getContext(), R.string.unsuccessful_schedule_add, Toast.LENGTH_LONG).show();
                                        Log.i("PROFESSOR ADD SCHEDULE", String.valueOf(response[0].code()));
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}
