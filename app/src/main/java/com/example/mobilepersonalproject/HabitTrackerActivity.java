package com.example.mobilepersonalproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilepersonalproject.adapters.HabitAdapter;
import com.example.mobilepersonalproject.models.Habit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitTrackerActivity extends AppCompatActivity {
    private EditText habitInput;
    private Button addHabitButton;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private RecyclerView habitRecyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList = new ArrayList<>();
    private ProgressBar habitProgressBar;
    private TextView habitProgressText;
    private SharedPreferences sharedPreferences;
    private static final String LAST_RESET_DATE_KEY = "last_reset_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        habitInput = findViewById(R.id.habit_input);
        addHabitButton = findViewById(R.id.add_habit_button);
        habitRecyclerView = findViewById(R.id.habit_recycler_view);
        habitProgressBar = findViewById(R.id.habit_progress_bar);
        habitProgressText = findViewById(R.id.habit_progress_text);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        sharedPreferences = getSharedPreferences("HabitPrefs", MODE_PRIVATE);

        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(habitList, this);
        habitRecyclerView.setAdapter(habitAdapter);

        addHabitButton.setOnClickListener(view -> addHabit());

        checkAndResetHabitsDaily(); // Reset habits if a new day
        loadHabits();
    }

    private void addHabit() {
        String habitName = habitInput.getText().toString().trim();
        if (habitName.isEmpty()) {
            Toast.makeText(this, "Enter a habit name", Toast.LENGTH_SHORT).show();
            return;
        }

        Habit habit = new Habit(habitName, false);

        if (user != null) {
            db.collection("users").document(user.getUid()).collection("habits").add(habit)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Habit Added!", Toast.LENGTH_SHORT).show();
                        habitList.add(habit);
                        habitAdapter.notifyItemInserted(habitList.size() - 1);
                        habitInput.setText("");
                        updateProgress();
                    });
        }
    }

    private void loadHabits() {
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("habits")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        habitList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Habit habit = doc.toObject(Habit.class);
                            habitList.add(habit);
                        }
                        habitAdapter.notifyDataSetChanged();
                        updateProgress(); // ✅ Ensure progress bar updates after loading data
                    });
        }
    }


    public void updateProgress() {
        int completedCount = 0;
        for (Habit habit : habitList) {
            if (habit.isCompleted()) completedCount++;
        }
        int progress = (habitList.size() > 0) ? (completedCount * 100 / habitList.size()) : 0;
        habitProgressBar.setProgress(progress);
        habitProgressText.setText("Daily Progress: " + progress + "%");
    }


    private void checkAndResetHabitsDaily() {
        String lastResetDate = sharedPreferences.getString(LAST_RESET_DATE_KEY, "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!lastResetDate.equals(todayDate)) {
            resetAllHabits();
            sharedPreferences.edit().putString(LAST_RESET_DATE_KEY, todayDate).apply();
        }
    }


    private void resetAllHabits() {
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("habits")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().update("completed", false);
                        }
                        loadHabits(); // ✅ Reload habits and update progress
                    });
        }
    }

}