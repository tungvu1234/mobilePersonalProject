package com.example.mobilepersonalproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.adapters.TrophyAdapter;
import com.example.mobilepersonalproject.models.Trophy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class GamificationFragment extends Fragment {
    private RecyclerView trophyRecyclerView;
    private TrophyAdapter trophyAdapter;
    private List<Trophy> trophyList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gamification, container, false);

        trophyRecyclerView = view.findViewById(R.id.trophy_recycler_view);
        trophyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        trophyAdapter = new TrophyAdapter(trophyList, getContext());
        trophyRecyclerView.setAdapter(trophyAdapter);

        loadTrophies();
        return view;
    }

    public void loadTrophies() {
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("trophies")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        trophyList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Trophy trophy = doc.toObject(Trophy.class);
                            trophyList.add(trophy);
                        }
                        trophyAdapter.notifyDataSetChanged(); // ✅ Update UI
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to load trophies", e));
        }
    }
}
