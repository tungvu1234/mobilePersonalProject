package com.example.mobilepersonalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.models.Exercise;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<Exercise> exerciseList;

    public ExerciseAdapter(Context context, List<Exercise> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.name.setText(exercise.getName());
        Glide.with(context).load(exercise.getGifUrl()).into(holder.gif);

        // Bind the instructions to the TextView
        StringBuilder instructions = new StringBuilder();
        for (String instruction : exercise.getInstructions()) {
            instructions.append(instruction).append("\n\n");
        }
        holder.instructions.setText(instructions.toString().trim());
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView name, instructions;
        ImageView gif;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.exercise_name);
            gif = itemView.findViewById(R.id.exercise_gif);
            instructions = itemView.findViewById(R.id.exercise_instructions);
        }
    }
}
