package com.example.mobilepersonalproject.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilepersonalproject.MainActivity;
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
import java.util.HashMap;
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
    private OnHabitUpdateListener habitUpdateListener; // Interface to notify MainActivity

    // 🔹 Define an interface to communicate with MainActivity
    public interface OnHabitUpdateListener {
        void onHabitUpdated();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnHabitUpdateListener) {
            habitUpdateListener = (OnHabitUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnHabitUpdateListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_tracker, container, false);

        habitInput = view.findViewById(R.id.habit_input);
        addHabitButton = view.findViewById(R.id.add_habit_button);
        habitRecyclerView = view.findViewById(R.id.habit_recycler_view);
        habitProgressBar = view.findViewById(R.id.habit_progress_bar);
        habitProgressText = view.findViewById(R.id.habit_progress_text);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        habitRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        habitAdapter = new HabitAdapter(habitList, getContext(), this);
        habitRecyclerView.setAdapter(habitAdapter);

        addHabitButton.setOnClickListener(view1 -> addHabit()); // Fix: Call correct method

        loadHabits(); // Fix: Call correct method to load stored habits
        return view;
    }


    private void updateProgress() {
        int completedCount = 0;
        for (Habit habit : habitList) {
            if (habit.isCompleted()) completedCount++;
        }
        int progress = (habitList.size() > 0) ? (completedCount * 100 / habitList.size()) : 0;
        habitProgressBar.setProgress(progress);
        habitProgressText.setText("Daily Progress: " + progress + "%");

        if (habitUpdateListener != null) {
            habitUpdateListener.onHabitUpdated(); // Notify MainActivity to update the calendar
        }
    }

    @Override
    public void onHabitCheckedChanged() {
        updateProgress(); // 🔹 Update progress bar

        if (habitUpdateListener != null) {
            habitUpdateListener.onHabitUpdated(); // Notify MainActivity to update calendar
        }

        markCalendar(); // 🔹 Mark the calendar based on new progress
    }

    private void markCalendar() {
        int completedCount = 0;
        for (Habit habit : habitList) {
            if (habit.isCompleted()) completedCount++;
        }

        int progress = (habitList.size() > 0) ? (completedCount * 100 / habitList.size()) : 0;
        String status = (progress == 100) ? "full" : (progress > 0 ? "half" : "none");

        Calendar today = Calendar.getInstance();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());

        if (user != null) {
            Map<String, Object> calendarEntry = new HashMap<>();
            calendarEntry.put("status", status);
            calendarEntry.put("date", todayDate);

            db.collection("users").document(user.getUid()).collection("calendar")
                    .document(todayDate)
                    .set(calendarEntry)
                    .addOnSuccessListener(aVoid -> {
                        if (habitUpdateListener != null) {
                            habitUpdateListener.onHabitUpdated(); // Update calendar view
                        }
                    });
        }
    }


    private void addHabit() {
        String habitName = habitInput.getText().toString().trim();
        if (habitName.isEmpty()) {
            return; // Prevent empty habit names
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
}
