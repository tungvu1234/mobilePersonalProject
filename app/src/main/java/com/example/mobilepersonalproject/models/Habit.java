package com.example.mobilepersonalproject.models;

public class Habit {
    private String habitName;
    private boolean isCompleted;

    public Habit() {}  // Empty constructor for Firestore

    public Habit(String habitName, boolean isCompleted) {
        this.habitName = habitName;
        this.isCompleted = isCompleted;
    }

    public String getHabitName() { return habitName; }
    public void setHabitName(String habitName) { this.habitName = habitName; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
