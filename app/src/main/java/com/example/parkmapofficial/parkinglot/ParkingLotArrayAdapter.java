package com.example.parkmapofficial.parkinglot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkmapofficial.MainActivity;
import com.example.parkmapofficial.R;
import com.example.parkmapofficial.me_ui.FeedbackActivity;

import java.util.ArrayList;

public class ParkingLotArrayAdapter extends
        RecyclerView.Adapter<ParkingLotArrayAdapter.ParkingLotViewHolder>
        implements Filterable {
    private Context context;
    private ArrayList<ParkingLot> mParkingLotList; // Real parking lot for adapter to track
    private ArrayList<ParkingLot> allParkingLot; // All parking lots from database
    private Activity activity;
    private View.OnClickListener onClickListener;

    private static final String ERROR_TAG = "ParkingLotArrayAdapter";

    public ParkingLotArrayAdapter(@NonNull Context context, Activity activity, View.OnClickListener onClickListener, ArrayList<ParkingLot> list) {
        this.mParkingLotList = list;
        this.allParkingLot = list;
        this.context = context;
        this.activity = activity;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ParkingLotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parkinglot_listitem, parent, false);
        return new ParkingLotViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingLotViewHolder holder, final int position) {
        String mName = mParkingLotList.get(position).getName();
        String mAddress = mParkingLotList.get(position).getAddress();
        holder.name.setText(mName);
        holder.address.setText(mAddress);
    }

    @Override
    public int getItemCount() {
        return mParkingLotList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<ParkingLot> filteredParkingList = new ArrayList<>();
                String constraintString = constraint.toString().toLowerCase().trim();
                if (constraintString.isEmpty()) {
                    filteredParkingList = allParkingLot;
                }
                else {
                    for (ParkingLot pl : allParkingLot)
                        if (pl.getTitle().toLowerCase().trim().contains(constraintString)
                                || pl.getSnippet().toLowerCase().trim().contains(constraintString))
                            filteredParkingList.add(pl);
                }
                FilterResults filterResults = new Filter.FilterResults();
                filterResults.count = filteredParkingList.size();
                filterResults.values = filteredParkingList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mParkingLotList = (ArrayList<ParkingLot>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ParkingLotViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener {
        ParkingLotArrayAdapter adapter;
        TextView name;
        TextView address;

        public ParkingLotViewHolder(@NonNull View itemView, ParkingLotArrayAdapter adapter) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.item_name);
            address = itemView.findViewById(R.id.item_address);
            this.adapter = adapter;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ParkingLotActivity.class);
            intent.putExtra("selectedLot", mParkingLotList.get(getAdapterPosition()));
            context.startActivity(intent);
        }
    }
}
