package com.example.parkmapofficial.parkinglot;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeocodingAPI {
    private static String urlString;
    private final static String ERROR_TAG = "GeocodingAPI";

    public GeocodingAPI(float latitude, float longitude) throws MalformedURLException {
        urlString = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + String.valueOf(latitude) + ","
                + String.valueOf(longitude)
                + "&key=AIzaSyCFyFZJpVa8EnACoBNCMCVkL7u4FNlYeZg";
    }

    private static String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
            sb.append(line + "\n");
        return sb.toString();
    }

    private static JSONObject readJsonFromUrl() throws IOException, JSONException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream in = connection.getInputStream();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            String json = readAll(reader);
            reader.close();
            in.close();
            return new JSONObject(json);
        } finally {
            connection.disconnect();
        }
    }

    public String getPlaceId() throws IOException, JSONException {
        JSONObject jsonObject = readJsonFromUrl();
        JSONArray results = jsonObject.getJSONArray("results");
        for (int i = 0; i < results.length(); ++i) {
            String placeId = ((JSONObject)results.get(i)).getString("place_id");
            if (placeId != null)
                return placeId;
        }
        return null;
    }
}
