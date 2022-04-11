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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    EditText usernameTextView, passwordTextView;
    Button loginButton, signupButton;

    ProgressBar progressBar;

    static final String PREF_FILE = "main_shared_preferences";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        usernameTextView = findViewById(R.id.login_username);
        passwordTextView = findViewById(R.id.login_password);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        progressBar = findViewById(R.id.login_progress_bar);

        progressBar.setVisibility(View.GONE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = usernameTextView.getText().toString();
                String password = passwordTextView.getText().toString();

                if (username.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.no_username, Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, R.string.no_password, Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("username", username);
                        params.put("password_hash", UserUtils.hashPassword(password));
                    } catch (JSONException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    String authority = getResources().getString(R.string.API_AUTHORITY);
                    String loginPath = getResources().getString(R.string.LOGIN_PATH);

                    progressBar.setVisibility(View.VISIBLE);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Looper uiLooper = Looper.getMainLooper();
                    Handler handler = new Handler(uiLooper);
                    final Response[] response = new Response[1];

                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                response[0] = UserUtils.queryAPI(authority + loginPath, params);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (response[0] == null) {
                                        Toast.makeText(LoginActivity.this, R.string.database_error, Toast.LENGTH_SHORT).show();
                                    } else if (response[0].code() == 200) {
                                        String result = null;
                                        try {
                                            result = response[0].body().string();
                                            JSONObject data = new JSONObject(result);
                                            Toast.makeText(LoginActivity.this, R.string.successful_login, Toast.LENGTH_SHORT).show();

                                            mPreferences = LoginActivity.this.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = mPreferences.edit();
                                            editor.putString("auth_string", data.getString("auth_string"));
                                            editor.apply();
                                            Toast.makeText(LoginActivity.this, R.string.successful_login, Toast.LENGTH_SHORT).show();

                                            Log.i("LOGIN", data.toString());
                                            if (data.getInt("state") == 1) {
                                                Intent intent = new Intent(LoginActivity.this, ProfessorHomeActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Intent intent = new Intent(LoginActivity.this, StudentHomeActivity.class);
                                                startActivity(intent);
                                            }
                                        } catch (IOException | JSONException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        Log.i("LOGIN", String.valueOf(response[0].code()));
                                    }

                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
                }
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                intent.putExtra("username", usernameTextView.getText().toString());
                intent.putExtra("password", passwordTextView.getText().toString());
                startActivity(intent);
            }
        });
    }
}
