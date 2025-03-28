package com.example.mobilepersonalproject;

import com.example.mobilepersonalproject.models.Exercise;

import java.util.List;

public class Response {
    private boolean success;
    private Data data;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}

