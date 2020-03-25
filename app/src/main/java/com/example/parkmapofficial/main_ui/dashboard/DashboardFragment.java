package com.example.parkmapofficial.main_ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkmapofficial.R;
import com.example.parkmapofficial.database.FirebaseDatabaseHelper;
import com.example.parkmapofficial.me_ui.FeedbackActivity;
import com.example.parkmapofficial.parkinglot.ParkingLot;
import com.example.parkmapofficial.parkinglot.ParkingLotArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private DashboardViewModel dashboardViewModel;

    // Search bar
    private EditText mSearchBar;
    private ImageView clearSearch;

    // Parking lot list
    private RecyclerView mParkingLotRecycler;
    private ParkingLotArrayAdapter mAdapter;
    private View.OnClickListener onClickListener;
    // Database
    private ArrayList<ParkingLot> mParkingLot = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //dashboardViewModel = new ViewModelProvider(getActivity()).get(DashboardViewModel.class);

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Search bar
        mSearchBar = root.findViewById(R.id.search_input);
        clearSearch = root.findViewById(R.id.search_clear);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchBar.setText("");
            }
        });

        // Setup RecyclerView
        mParkingLotRecycler = root.findViewById(R.id.park_list);
        mAdapter = new ParkingLotArrayAdapter(Objects.requireNonNull(getContext()),
                Objects.requireNonNull(getActivity()).getParent(),
                onClickListener, mParkingLot);
        mParkingLotRecycler.setAdapter(mAdapter);
        mParkingLotRecycler.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        setupParkingLotDatabase();
        mAdapter.notifyDataSetChanged();

        // Set up filter search
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mAdapter != null)
                    mAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mAdapter != null)
                    mAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString();
                mAdapter.getFilter().filter(query);
            }
        });

        // Set up filter when keyboard search clicked
        mSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mAdapter.getFilter().filter(mSearchBar.getText());
                }
                return false;
            }
        });

        return root;
    }

    private void setupParkingLotDatabase() {
        new FirebaseDatabaseHelper().readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                mParkingLot.addAll(parkingLots);
                mAdapter.notifyDataSetChanged();
            }
            @Override
            public void DataIsInserted() {

            }
            @Override
            public void DataIsUpdated() {

            }
            @Override
            public void DataIsDeleted() {

            }
        });
    }
}
