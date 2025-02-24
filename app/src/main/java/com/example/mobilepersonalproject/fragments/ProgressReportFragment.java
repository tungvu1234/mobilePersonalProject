package com.example.mobilepersonalproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.mobilepersonalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.graphics.drawable.Drawable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressReportFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseUser user;
    private CalendarView calendarView;
    private List<EventDay> events = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress_report, container, false);

        calendarView = view.findViewById(R.id.calendar_view);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadCalendarMarks(); // Load stored calendar data

        return view;
    }

    public void loadCalendarMarks() {
        if (user == null) {
            Log.e("Firestore", "User is not authenticated!");
            return;
        }

        db.collection("users").document(user.getUid()).collection("calendar")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    events.clear(); // Clear old events before reloading

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String dateStr = doc.getString("date");

                        try {
                            // Convert stored Firestore date String to Date object
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

                    // Refresh calendar UI
                    calendarView.setEvents(events);
                });
    }
}
