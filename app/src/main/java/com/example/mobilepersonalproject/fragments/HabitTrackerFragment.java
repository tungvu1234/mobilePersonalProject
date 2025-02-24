package com.example.mobilepersonalproject.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.adapters.HabitAdapter;
import com.example.mobilepersonalproject.models.Habit;
import com.example.mobilepersonalproject.models.Trophy;
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
        checkAndAwardTrophy();

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

        // ✅ Correctly format the date before storing it in Firestore
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        if (user != null) {
            Map<String, Object> calendarEntry = new HashMap<>();
            calendarEntry.put("status", status);
            calendarEntry.put("date", todayDate);

            db.collection("users").document(user.getUid()).collection("calendar")
                    .document(todayDate)
                    .set(calendarEntry)
                    .addOnSuccessListener(aVoid -> {
                        if (habitUpdateListener != null) {
                            habitUpdateListener.onHabitUpdated(); // 🔹 Ensure calendar updates
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

    private void awardTrophy(String name, String description, String imageUrl) {
        if (user != null) {
            Trophy newTrophy = new Trophy(name, description, imageUrl);
            db.collection("users").document(user.getUid()).collection("trophies")
                    .add(newTrophy)
                    .addOnSuccessListener(documentReference -> {
                        sendTrophyNotification(name);
                        updateGamificationFragment();
                    });
        }
    }

    private void sendTrophyNotification(String trophyName) {
        String channelId = "trophy_channel"; // ✅ Ensure channel ID is consistent

        // ✅ Check if notification permission is granted (Only needed for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return; // Don't send the notification if permission is not granted
            }
        }

        // ✅ Create the notification channel (Only needed for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Trophy Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for earned trophies.");

            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // ✅ Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), channelId)
                .setSmallIcon(R.drawable.trophy_icon)
                .setContentTitle("New Trophy Unlocked!")
                .setContentText("You earned the '" + trophyName + "' trophy!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        notificationManager.notify(100, builder.build()); // ✅ Now safe to call
    }

    private void updateGamificationFragment() {
        if (getActivity() != null) {
            GamificationFragment gamificationFragment = (GamificationFragment) getActivity()
                    .getSupportFragmentManager().findFragmentByTag("GAMIFICATION_FRAGMENT");
            if (gamificationFragment != null) {
                gamificationFragment.loadTrophies(); // 🔹 Update the reward shelf
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) { // 🔹 Match the request code in sendTrophyNotification()
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 🔹 Permission granted! Send the notification now
                sendTrophyNotification("Daily Master");
            } else {
                // 🔹 Permission denied. Show a message or handle it gracefully
                Toast.makeText(getContext(), "Notification permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndAwardTrophy() {
        int completedCount = 0;
        for (Habit habit : habitList) {
            if (habit.isCompleted()) completedCount++;
        }

        // Check if all habits are completed for today
        if (completedCount == habitList.size() && habitList.size() > 0) {
            awardTrophy("Daily Master", "Completed all daily habits!", "https://cdn-icons-png.flaticon.com/512/616/616490.png");
            checkStreakAchievement();
        }
    }

    private void checkStreakAchievement() {
        if (user == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayDate = sdf.format(yesterday.getTime());

        db.collection("users").document(user.getUid()).collection("calendar")
                .document(yesterdayDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("status");

                        // 🔹 If all habits were completed yesterday, award the streak trophy
                        if ("full".equals(status)) {
                            awardTrophy("Streak Master", "Completed all daily habits for 2 consecutive days!", "https://cdn-icons-png.flaticon.com/512/1824/1824251.png");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to check streak", e));
    }

}
