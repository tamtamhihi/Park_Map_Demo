package com.example.parkmapofficial.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

public class CustomSharedPreferences {
    private SharedPreferences sharedPref;
    private String fileName = "com.example.parkmapofficial";
    private String NIGHT_MODE_KEY = "NIGHT_MODE";
    private String LANGUAGE_KEY = "LANGUAGE";

    public CustomSharedPreferences(Context context) {
        sharedPref = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public void setLanguage(String lang) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LANGUAGE_KEY, lang);
        editor.apply();
    }

    public String getLanguage() {
        String lang = sharedPref.getString(LANGUAGE_KEY, "English");
        return lang;
    }

    public void setNightModeState(Boolean isNightMode) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(NIGHT_MODE_KEY, isNightMode);
        editor.apply();
    }

    public Boolean getNightModeState() {
        Boolean isNightMode = sharedPref.getBoolean(NIGHT_MODE_KEY, false);
        return isNightMode;
    }
}
