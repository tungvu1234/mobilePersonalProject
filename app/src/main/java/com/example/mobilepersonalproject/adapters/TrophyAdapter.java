package com.example.mobilepersonalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mobilepersonalproject.R;
import com.example.mobilepersonalproject.models.Trophy;
import java.util.List;

public class TrophyAdapter extends RecyclerView.Adapter<TrophyAdapter.ViewHolder> {
    private List<Trophy> trophyList;
    private Context context;

    public TrophyAdapter(List<Trophy> trophyList, Context context) {
        this.trophyList = trophyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trophy, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trophy trophy = trophyList.get(position);
        holder.trophyName.setText(trophy.getName());
        holder.trophyDescription.setText(trophy.getDescription());

        Glide.with(context).load(trophy.getImageUrl()).into(holder.trophyImage);
    }

    @Override
    public int getItemCount() {
        return trophyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView trophyImage;
        TextView trophyName, trophyDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            trophyImage = itemView.findViewById(R.id.trophy_image);
            trophyName = itemView.findViewById(R.id.trophy_name);
            trophyDescription = itemView.findViewById(R.id.trophy_description);
        }
    }
}
