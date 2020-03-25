package com.example.parkmapofficial.main_ui.me;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.parkmapofficial.MainActivity;
import com.example.parkmapofficial.R;
import com.example.parkmapofficial.sharedpref.CustomSharedPreferences;

import java.util.Locale;
import java.util.Objects;

public class MeFragment extends Fragment {

    private MeViewModel notificationsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);
        return root;
    }
}
