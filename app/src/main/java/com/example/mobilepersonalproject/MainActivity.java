package com.example.mobilepersonalproject;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.mobilepersonalproject.fragments.HabitTrackerFragment;
import com.example.mobilepersonalproject.fragments.ExercisePlanFragment;
import com.example.mobilepersonalproject.fragments.ProgressReportFragment;
import com.example.mobilepersonalproject.fragments.GamificationFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HabitTrackerFragment()).commit();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.nav_habits) {
                        selectedFragment = new HabitTrackerFragment();
                    } else if (item.getItemId() == R.id.nav_exercise) {
                        selectedFragment = new ExercisePlanFragment();
                    } else if (item.getItemId() == R.id.nav_progress) {
                        selectedFragment = new ProgressReportFragment();
                    } else if (item.getItemId() == R.id.nav_gamification) {
                        selectedFragment = new GamificationFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment).commit();
                    }
                    return true;
                }
            };

}
