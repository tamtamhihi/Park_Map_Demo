package com.example.parkmapofficial.parkinglot;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.parkmapofficial.R;
import com.example.parkmapofficial.sharedpref.CustomSharedPreferences;
import com.example.parkmapofficial.userrating.UserRating;
import com.example.parkmapofficial.userrating.UserRatingAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParkingLotActivity extends AppCompatActivity {

    private ParkingLot item;
    private String ERROR_TAG = ParkingLotActivity.class.getSimpleName();

    // App mode
    private CustomSharedPreferences appMode;

    // Collapsing toolbar
    private Toolbar toolbar;
    private ViewPager viewPager;
    private String[] imageUrls = new String[]{
            "https://cdn.pixabay.com/photo/2016/11/11/23/34/cat-1817970_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/12/21/12/26/glowworm-3031704_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/12/24/09/09/road-3036620_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/11/07/00/07/fantasy-2925250_960_720.jpg",
            "https://cdn.pixabay.com/photo/2017/10/10/15/28/butterfly-2837589_960_720.jpg"
    };
    private ArrayList<Bitmap> imageBitmaps = new ArrayList<>();

    // Layout Views
    private TextView name;
    private TextView address;
    private RatingBar rating;
    private TextView price;
    private RecyclerView userRating;
    private TextView mRatingEmpty;


    private GeocodingAPI geocodingAPI;
    String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appMode = new CustomSharedPreferences(this);
        if (appMode.getNightModeState())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_parking_lot);

        // Current parking lot
        item = (ParkingLot) getIntent().getSerializableExtra("selectedLot");

        // Get all information
        String mName = item.getName();
        String mAddress = item.getAddress();
        float mRating = item.getAverageRating();
        String mPrice = item.getPrice();
        ArrayList<UserRating> mUserRating = item.getUserRatings();


//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (geocodingAPI != null) {
//                    try {
//                        placeId = geocodingAPI.getPlaceId();
//                        Log.d("place_in_theard", placeId);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                } else Toast.makeText(ParkingLotActivity.this,"tu cho nay",Toast.LENGTH_SHORT).show();
//            }
//        });
//        thread.start();
        new getplaceid().execute();



        // Get metadata

        // Get views in app bar layout
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mName);
        viewPager = findViewById(R.id.gallery_swipe);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, imageBitmaps);
        viewPager.setAdapter(adapter);

        // Remaining views
        name = findViewById(R.id.title);
        address = findViewById(R.id.address);
        rating = findViewById(R.id.rating);
        price = findViewById(R.id.price);
        userRating = findViewById(R.id.user_rating_list);
        mRatingEmpty = findViewById(R.id.comment_not_available);

        // Set information to views
        toolbar.setTitle(mName);
        toolbar.setSubtitle(mAddress);
        name.setText(mName);
        address.setText(mAddress);
        rating.setRating(mRating);

        if (mPrice != null) price.setText(mPrice);
        else price.setText(R.string.not_available_field);

        if (mUserRating == null || mUserRating.size() == 0) {
            mRatingEmpty.setVisibility(View.VISIBLE);
        } else {
            rating.setRating(mRating);
            // Set up rating recycler view
            UserRatingAdapter mAdapter = new UserRatingAdapter(mUserRating);
            userRating.setAdapter(mAdapter);
            userRating.setLayoutManager(new LinearLayoutManager(this));
        }

        Log.d("duma", "on Create of parking lot activity finished");
    }

    private class getplaceid extends AsyncTask<Void,GeocodingAPI,String>{
        @Override
        protected String doInBackground(Void... voids) {
            // Fetch place ID
            try {
                geocodingAPI = new GeocodingAPI(item.getLatitude(), item.getLongitude());
            } catch (MalformedURLException e) {
                Log.e(ERROR_TAG, "MalformedURLException: " + e);
            }
            try {
                return geocodingAPI.getPlaceId();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            placeId = s;

            // Initialize places client
            Places.initialize(ParkingLotActivity.this, getString(R.string.google_api_key));
            final PlacesClient client = Places.createClient(ParkingLotActivity.this);

            if (placeId != null) {
                List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
                Log.d(ERROR_TAG, "Place ID: " + placeId);
                FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);
                client.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Log.d("client_ok", "danhl");
                        Place place = fetchPlaceResponse.getPlace();
                        List<PhotoMetadata> photoMetadatas = place.getPhotoMetadatas();
                        assert photoMetadatas != null;

                        for (PhotoMetadata photo : photoMetadatas) {

                            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
                            client.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                                @Override
                                public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                    imageBitmaps.add(bitmap);
                                    Toast.makeText(ParkingLotActivity.this, "succed", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof ApiException) {
                                        ApiException exception = (ApiException) e;
                                        Log.e(ERROR_TAG, "Api exception: " + e);
                                        Toast.makeText(ParkingLotActivity.this, "fail :((", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(ERROR_TAG, "Exception: " + e.getMessage());
                        Log.d("Fail2","Nah~");
                    }
                });
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
    }
}

