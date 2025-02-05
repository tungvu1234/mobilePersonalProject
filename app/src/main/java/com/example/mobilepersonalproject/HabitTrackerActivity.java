package com.example.mobilepersonalproject;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.mobilepersonalproject.adapters.HabitAdapter;
import com.example.mobilepersonalproject.models.Habit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private CalendarView calendarView;
    private List<EventDay> events = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        habitInput = findViewById(R.id.habit_input);
        addHabitButton = findViewById(R.id.add_habit_button);
        habitRecyclerView = findViewById(R.id.habit_recycler_view);
        habitProgressBar = findViewById(R.id.habit_progress_bar);
        habitProgressText = findViewById(R.id.habit_progress_text);
        calendarView = findViewById(R.id.calendar_view);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        habitRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(habitList, this);
        habitRecyclerView.setAdapter(habitAdapter);

        addHabitButton.setOnClickListener(view -> addHabit());

        loadHabits();
        loadCalendarMarks();
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
                        updateProgress();
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

        // 🔹 Update Firestore and refresh the calendar
        if (progress == 100) {
            markCalendar("full");
        } else if (progress > 0) {
            markCalendar("half");
        } else {
            markCalendar("none");
        }

        // 🔹 Refresh Calendar after updating Firestore
        loadCalendarMarks();
    }


    private void markCalendar(String status) {
        if (user == null) {
            Log.e("Firestore", "User is not authenticated!");
            return;
        }

        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(today.getTime()); // ✅ Ensure correct date format

        // ✅ Remove old events before adding a new one
        events.removeIf(event -> event.getCalendar().getTime().equals(today.getTime()));

        int drawableId = status.equals("full") ? R.drawable.full_circle :
                status.equals("half") ? R.drawable.half_circle : 0;

        if (drawableId != 0) {
            Drawable drawable = getResources().getDrawable(drawableId);
            events.add(new EventDay(today, drawable));
        }

        calendarView.setEvents(events);  // 🔹 Force UI Update

        // ✅ Save the date in Firestore
        Map<String, Object> calendarEntry = new HashMap<>();
        calendarEntry.put("status", status);
        calendarEntry.put("date", todayDate); // 🔹 Store formatted date

        db.collection("users").document(user.getUid()).collection("calendar")
                .document(todayDate)
                .set(calendarEntry)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Calendar entry updated successfully");
                    runOnUiThread(() -> {
                        loadCalendarMarks(); // 🔹 Force Refresh on Main UI Thread
                    });
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update calendar entry", e));
    }






    private void loadCalendarMarks() {
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("calendar")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        events.clear(); // 🔹 Clear old events before reloading

                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String dateStr = doc.getString("date");

                            try {
                                // ✅ Convert String to Date properly
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date parsedDate = sdf.parse(dateStr);

                                if (parsedDate != null) {
                                    Calendar date = Calendar.getInstance();
                                    date.setTime(parsedDate);

                                    String status = doc.getString("status");
                                    int drawableId = status.equals("full") ? R.drawable.full_circle :
                                            status.equals("half") ? R.drawable.half_circle : 0;

                                    if (drawableId != 0) {
                                        Drawable drawable = getResources().getDrawable(drawableId);
                                        events.add(new EventDay(date, drawable));
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("Firestore", "Error parsing date: " + dateStr, e);
                            }
                        }

                        // 🔹 Update UI on the Main Thread
                        runOnUiThread(() -> {
                            calendarView.setEvents(events);
                        });
                    });
        }
    }
}