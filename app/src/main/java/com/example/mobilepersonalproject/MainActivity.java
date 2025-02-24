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

public class MainActivity extends AppCompatActivity implements HabitTrackerFragment.OnHabitUpdateListener {
    private ProgressReportFragment progressReportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Initialize ProgressReportFragment
        progressReportFragment = new ProgressReportFragment();

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HabitTrackerFragment()).commit();
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_habits) {
                    selectedFragment = new HabitTrackerFragment();
                } else if (item.getItemId() == R.id.nav_exercise) {
                    selectedFragment = new ExercisePlanFragment();
                } else if (item.getItemId() == R.id.nav_progress) {
                    selectedFragment = progressReportFragment; // Use stored reference
                } else if (item.getItemId() == R.id.nav_gamification) {
                    selectedFragment = new GamificationFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            };

    @Override
    public void onHabitUpdated() {
        if (progressReportFragment != null) {
            progressReportFragment.loadCalendarMarks(); // Refresh calendar when progress changes
        }
    }
}
