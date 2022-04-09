package com.example.infosys1d;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseUtils {
    static JSONObject getStudentData(Context context) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        JSONObject params = new JSONObject();
        params.put("auth_string", mPreferences.getString("auth_string", ""));

        String authority = context.getResources().getString(R.string.API_AUTHORITY);
        String studentPath = context.getResources().getString(R.string.STUDENT_PATH);
        String schedulePath = context.getResources().getString(R.string.SCHEDULE_PATH);

        RequestBody body = RequestBody.create(JSON, params.toString());
        Request request = new Request.Builder()
                .url(authority + studentPath + schedulePath)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            String result = response.body().string();
            return new JSONObject(result);

        } else if (response.code() == 418){
            Log.i("DATABASE", "Invalid auth string.");
        } else {
            Log.i("DATABASE", response.body().string());
        }
        return null;
    }

    static JSONObject getProfessorData(Context context) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        final String PREF_FILE = "main_shared_preferences";
        SharedPreferences mPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        JSONObject params = new JSONObject();
        params.put("auth_string", mPreferences.getString("auth_string", ""));

        String authority = context.getResources().getString(R.string.API_AUTHORITY);
        String professorPath = context.getResources().getString(R.string.FACULTY_PATH);
        String schedulePath = context.getResources().getString(R.string.SCHEDULE_PATH);

        RequestBody body = RequestBody.create(JSON, params.toString());
        Request request = new Request.Builder()
                .url(authority + professorPath + schedulePath)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            String result = response.body().string();
            return new JSONObject(result);

        } else if (response.code() == 418){
            Log.i("DATABASE", "Invalid auth string.");
        } else {
            Log.i("DATABASE", response.body().string());
        }
        return null;
    }
}