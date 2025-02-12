package com.example.mobilepersonalproject.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.adapters.HabitAdapter;
import com.example.mobilepersonalproject.models.Habit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HabitTrackerFragment extends Fragment implements HabitAdapter.OnHabitCheckedChangeListener {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_tracker, container, false);

        habitInput = view.findViewById(R.id.habit_input);
        addHabitButton = view.findViewById(R.id.add_habit_button);
        habitRecyclerView = view.findViewById(R.id.habit_recycler_view);
        habitProgressBar = view.findViewById(R.id.habit_progress_bar);
        habitProgressText = view.findViewById(R.id.habit_progress_text);
        calendarView = view.findViewById(R.id.calendar_view);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        habitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        habitAdapter = new HabitAdapter(habitList, getContext(), this); // 🔹 Pass fragment as listener
        habitRecyclerView.setAdapter(habitAdapter);

        addHabitButton.setOnClickListener(view1 -> addHabit());

        loadHabits();
        loadCalendarMarks();
        return view;
    }


    private void addHabit() {
        String habitName = habitInput.getText().toString().trim();
        if (habitName.isEmpty()) {
            return;
        }

        Habit habit = new Habit(habitName, false);

        if (user != null) {
            db.collection("users").document(user.getUid()).collection("habits").add(habit)
                    .addOnSuccessListener(documentReference -> {
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

    private void updateProgress() {
        int completedCount = 0;
        for (Habit habit : habitList) {
            if (habit.isCompleted()) completedCount++;
        }
        int progress = (habitList.size() > 0) ? (completedCount * 100 / habitList.size()) : 0;
        habitProgressBar.setProgress(progress);
        habitProgressText.setText("Daily Progress: " + progress + "%");

        markCalendar(progress == 100 ? "full" : (progress > 0 ? "half" : "none"));
    }

    private void loadCalendarMarks() {
        if (user == null) {
            Log.e("Firestore", "User is not authenticated!");
            return;
        }

        db.collection("users").document(user.getUid()).collection("calendar")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    events.clear(); // 🔹 Clear old events before reloading

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String dateStr = doc.getString("date");

                        try {
                            // ✅ Convert stored Firestore date String to Date object
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

                    // 🔹 Refresh calendar UI
                    calendarView.setEvents(events);
                });
    }


    private void markCalendar(String status) {
        Calendar today = Calendar.getInstance();
        String todayDate = dateFormat.format(today.getTime());

        int drawableId = status.equals("full") ? R.drawable.full_circle :
                status.equals("half") ? R.drawable.half_circle : 0;

        if (drawableId != 0) {
            db.collection("users").document(user.getUid()).collection("calendar")
                    .document(todayDate)
                    .set(Map.of("status", status, "date", todayDate));
        }
    }
    @Override
    public void onHabitCheckedChanged() {
        updateProgress(); // 🔹 Update progress and calendar when a habit is checked
        loadCalendarMarks(); // 🔹 Refresh calendar in real-time
    }
}
