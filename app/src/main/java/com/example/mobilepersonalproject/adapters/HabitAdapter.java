package com.example.mobilepersonalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilepersonalproject.HabitTrackerActivity;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.models.Habit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {
    private List<Habit> habitList;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private HabitTrackerActivity activity;

    public HabitAdapter(List<Habit> habitList, HabitTrackerActivity activity) {
        this.habitList = habitList;
        this.activity = activity;
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.habitName.setText(habit.getHabitName());
        holder.habitCheckbox.setOnCheckedChangeListener(null); // Prevents unwanted triggers
        holder.habitCheckbox.setChecked(habit.isCompleted());

        // ✅ Update Firestore when checkbox is toggled
        holder.habitCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habit.setCompleted(isChecked);

            if (user != null) {
                String userId = user.getUid();
                db.collection("users").document(userId)
                        .collection("habits")
                        .whereEqualTo("habitName", habit.getHabitName())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference()
                                        .update("completed", isChecked)
                                        .addOnSuccessListener(aVoid -> activity.updateProgress());
                            }
                        });
            }
        });

        // ✅ Delete habit from Firestore
        holder.deleteHabitButton.setOnClickListener(v -> {
            if (user != null) {
                String userId = user.getUid();
                db.collection("users").document(userId)
                        .collection("habits")
                        .whereEqualTo("habitName", habit.getHabitName())
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            habitList.remove(position);
                                            notifyItemRemoved(position);
                                            activity.updateProgress();
                                        });
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox habitCheckbox;
        TextView habitName;
        ImageButton deleteHabitButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            habitCheckbox = itemView.findViewById(R.id.habit_checkbox);
            habitName = itemView.findViewById(R.id.habit_name);
            deleteHabitButton = itemView.findViewById(R.id.delete_habit_button);
        }
    }
}