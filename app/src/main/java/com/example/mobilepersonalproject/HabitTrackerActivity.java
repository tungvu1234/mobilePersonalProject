package com.example.mobilepersonalproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilepersonalproject.models.Habit;
import com.example.mobilepersonalproject.adapters.HabitAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HabitTrackerActivity extends AppCompatActivity {
    private EditText habitInput;
    private Button addHabitButton;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private RecyclerView habitRecyclerView;
    private HabitAdapter habitAdapter;
    private List<Habit> habitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        habitInput = findViewById(R.id.habit_input);
        addHabitButton = findViewById(R.id.add_habit_button);
        habitRecyclerView = findViewById(R.id.habit_recycler_view);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up RecyclerView
        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(habitList);
        habitRecyclerView.setAdapter(habitAdapter);

        // Add habit on button click
        addHabitButton.setOnClickListener(view -> addHabit());

        // Load habits from Firebase
        loadHabits();
    }

    private void addHabit() {
        String habitName = habitInput.getText().toString();

        if (!habitName.isEmpty()) {
            Habit habit = new Habit(habitName, false);

            db.collection("users").document(user.getUid()).collection("habits")
                    .add(habit)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Habit Added!", Toast.LENGTH_SHORT).show();

                        // 🟢 Add new habit to the list and update RecyclerView immediately
                        habitList.add(habit);
                        habitAdapter.notifyItemInserted(habitList.size() - 1);

                        // Clear input field after adding a habit
                        habitInput.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add habit: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "Enter a habit name", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadHabits() {
        db.collection("users").document(user.getUid()).collection("habits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    habitList.clear(); // 🟢 Clear list to avoid duplicates

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Habit habit = doc.toObject(Habit.class);
                        habitList.add(habit);
                    }

                    // 🟢 Notify adapter after data changes
                    habitAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load habits: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}