package com.example.parkmapofficial;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.parkmapofficial.main_ui.dashboard.DashboardFragment;
import com.example.parkmapofficial.main_ui.maps.MapsFragment;
import com.example.parkmapofficial.main_ui.me.MeFragment;
import com.example.parkmapofficial.me_ui.AboutActivity;
import com.example.parkmapofficial.me_ui.FeedbackActivity;
import com.example.parkmapofficial.sharedpref.CustomSharedPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Shared Preferences for Language and Themes
    private CustomSharedPreferences appMode;

    // Fragments for setting up bottom navigation
    final Fragment mapsFragment = new MapsFragment();
    final Fragment dashboardFragment = new DashboardFragment();
    final Fragment meFragment = new MeFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment activeFragment;
    private LinearLayout settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Getting shared preferences for language and themes
        appMode = new CustomSharedPreferences(this);
        if (appMode.getLanguage().equals("English"))
            changeLocale("en");
        else
            changeLocale("vi");
        if (appMode.getNightModeState())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        settings = (LinearLayout)findViewById(R.id.settings);

        // Setting up bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_maps:
                        fm.beginTransaction().hide(activeFragment).show(mapsFragment).commit();
                        activeFragment = mapsFragment;
                        settings.setVisibility(View.GONE);
                        return true;
                    case R.id.navigation_dashboard:
                        fm.beginTransaction().hide(activeFragment).show(dashboardFragment).commit();
                        activeFragment = dashboardFragment;
                        settings.setVisibility(View.GONE);
                        return true;
                    case R.id.navigation_me:
                        fm.beginTransaction().hide(activeFragment).show(meFragment).commit();
                        activeFragment = meFragment;
                        settings.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });

        // Add all fragments but show only maps fragment
        fm.beginTransaction().add(R.id.nav_host_fragment, dashboardFragment, "2").hide(dashboardFragment).commit();
        if (savedInstanceState != null) {
            fm.beginTransaction().add(R.id.nav_host_fragment, mapsFragment, "1").hide(mapsFragment).commit();
            fm.beginTransaction().add(R.id.nav_host_fragment, meFragment, "3").commit();
            settings.setVisibility(View.VISIBLE);
            activeFragment = meFragment;
        }
        else {
            fm.beginTransaction().add(R.id.nav_host_fragment, mapsFragment, "1").commit();
            fm.beginTransaction().add(R.id.nav_host_fragment, meFragment, "3").hide(meFragment).commit();
            activeFragment = mapsFragment;
        }

        // Language settings
        CardView language = findViewById(R.id.language);
        TextView langIndicator = findViewById(R.id.lang_indicator);
        if (appMode.getLanguage().equals("Vietnamese"))
            langIndicator.setText("Tiếng Việt");
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this, R.style.AppDialog);
                dialog.setContentView(R.layout.language_dialog);
                final RadioGroup languageSelector = dialog.findViewById(R.id.lang_selector);
                if (appMode.getLanguage().equals("English"))
                    languageSelector.check(R.id.en);
                else
                    languageSelector.check(R.id.vi);
                languageSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.en:
                                appMode.setLanguage("English");
                                break;
                            case R.id.vi:
                                appMode.setLanguage("Vietnamese");
                                break;
                        }
                        MainActivity.this.recreate();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        // Night mode settings
        Switch nightMode = findViewById(R.id.night_mode_switch);
        if (appMode.getNightModeState())
            nightMode.setChecked(true);
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    appMode.setNightModeState(true);
                else
                    appMode.setNightModeState(false);
                MainActivity.this.recreate();
            }
        });

        // Feedback
        CardView feedback = findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            }
        });

        // About
        CardView about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("KEY", true);
        super.onSaveInstanceState(outState);
    }

    private void changeLocale(String lang) {
        Locale locale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, dm);
    }
}