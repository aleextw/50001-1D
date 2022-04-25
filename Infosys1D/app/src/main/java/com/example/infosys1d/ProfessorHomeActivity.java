package com.example.infosys1d;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.StrictMode;
import android.view.MenuItem;

public class ProfessorHomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener  {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_home);

        // Not necessary; safeguard in case Android thread policy acts up
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Hide action bar
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.schedule);

    }
    // Setup our fragments
    ProfessorScheduleFragment professorScheduleFragment = new ProfessorScheduleFragment();
    ProfessorModuleFragment professorModuleFragment = new ProfessorModuleFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle changing of our navigation bar's selected item
        switch (item.getItemId()) {
            case R.id.schedule:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, professorScheduleFragment).commit();
                return true;

            case R.id.modules:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, professorModuleFragment).commit();
                return true;

            case R.id.profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, profileFragment).commit();
                return true;
        }
        return false;
    }
}

