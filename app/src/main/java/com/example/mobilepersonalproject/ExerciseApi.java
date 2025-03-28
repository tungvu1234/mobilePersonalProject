package com.example.mobilepersonalproject;

import com.example.mobilepersonalproject.models.Exercise;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface ExerciseApi {
    @Headers("X-RapidAPI-Key: ef797056bdmsh020c624b6ed660ap16d698jsn5a61f51f90d1")
    @GET("https://exercisedb.p.rapidapi.com/exercises")
    Call<List<Exercise>> getAllExercises();
}

