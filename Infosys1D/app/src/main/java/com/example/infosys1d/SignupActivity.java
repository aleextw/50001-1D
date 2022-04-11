package com.example.infosys1d;

import static com.example.infosys1d.UserUtils.mPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {
    TextView usernameTextView, firstNameTextView, lastNameTextView, passwordTextView, confirmPasswordTextView, backTextView, signupTextView;
    Switch statusSwitch;
    ProgressBar progressBar;
    final boolean[] status = new boolean[1];
    static final String PREF_FILE = "main_shared_preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_signup);
        getSupportActionBar().hide();

        usernameTextView = findViewById(R.id.signup_username);
        firstNameTextView = findViewById(R.id.signup_first_name);
        lastNameTextView = findViewById(R.id.signup_last_name);
        passwordTextView = findViewById(R.id.signup_password);
        confirmPasswordTextView = findViewById(R.id.signup_confirm_password);
        backTextView = findViewById(R.id.back_button);
        signupTextView = findViewById(R.id.signup_button);
        statusSwitch = findViewById(R.id.status_switch);
        progressBar = findViewById(R.id.signup_progress_bar);

        Intent loginIntent = getIntent();

        usernameTextView.setText(loginIntent.getStringExtra("username"));
        passwordTextView.setText(loginIntent.getStringExtra("password"));

        progressBar.setVisibility(View.GONE);

        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                status[0] = isChecked;
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameTextView.getText().toString();
                String firstName = firstNameTextView.getText().toString();
                String lastName = lastNameTextView.getText().toString();
                String password = passwordTextView.getText().toString();
                String confirmPassword = confirmPasswordTextView.getText().toString();

                // TODO: Replace with proper names
                // TODO: Shift POST request to secondary method and keep checks in primary method
                if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
                        password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(SignupActivity.this, R.string.empty_field, Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupActivity.this, R.string.unmatched_passwords, Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("username", username);
                        params.put("password_hash", UserUtils.hashPassword(password));
                        params.put("first_name", firstName);
                        params.put("last_name", lastName);
                        params.put("status", status[0] ? 1 : 0);
                    } catch (JSONException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    String authority = getResources().getString(R.string.API_AUTHORITY);
                    String signupPath = getResources().getString(R.string.SIGNUP_PATH);

                    progressBar.setVisibility(View.VISIBLE);

                    Response[] response = new Response[1];

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Looper uiLooper = Looper.getMainLooper();
                    Handler handler = new Handler(uiLooper);

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                response[0] = UserUtils.queryAPI(authority + signupPath, params);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (response[0] == null) {
                                        Toast.makeText(SignupActivity.this, R.string.database_error, Toast.LENGTH_SHORT).show();
                                    } else if (response[0].code() == 200) {
                                        String result = null;
                                        try {
                                            result = response[0].body().string();
                                            JSONObject data = new JSONObject(result);
                                            Toast.makeText(SignupActivity.this, R.string.successful_login, Toast.LENGTH_SHORT).show();

                                            mPreferences = SignupActivity.this.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = mPreferences.edit();
                                            editor.putString("auth_string", data.getString("auth_string"));
                                            editor.apply();
                                            Toast.makeText(SignupActivity.this, R.string.successful_login, Toast.LENGTH_SHORT).show();

                                            Log.i("SIGNUP", data.toString());
                                            if (data.getInt("state") == 1) {
                                                Intent intent = new Intent(SignupActivity.this, ProfessorHomeActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(SignupActivity.this, StudentHomeActivity.class);
                                                startActivity(intent);
                                            }
                                        } catch (IOException | JSONException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        Log.i("SIGNUP", String.valueOf(response[0].code()));
                                    }

                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            }
        });

        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
}
