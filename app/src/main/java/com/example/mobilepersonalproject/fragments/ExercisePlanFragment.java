package com.example.mobilepersonalproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilepersonalproject.ExerciseApi;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.Response;
import com.example.mobilepersonalproject.RetrofitClient;
import com.example.mobilepersonalproject.adapters.ExerciseAdapter;
import com.example.mobilepersonalproject.models.Exercise;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class ExercisePlanFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exerciseList;

    public ExercisePlanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_plan, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchExercises(); // Fetch and display exercises

        return view;
    }

    private void fetchExercises() {
        ExerciseApi api = RetrofitClient.getInstance().create(ExerciseApi.class);
        Call<List<Exercise>> call = api.getAllExercises();

        call.enqueue(new Callback<List<Exercise>>() {
            @Override
            public void onResponse(Call<List<Exercise>> call, retrofit2.Response<List<Exercise>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    exerciseList = response.body();
                    exerciseAdapter = new ExerciseAdapter(getContext(), exerciseList);
                    recyclerView.setAdapter(exerciseAdapter);
                } else {
                    Toast.makeText(getContext(), "Failed to load exercises", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<List<Exercise>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

