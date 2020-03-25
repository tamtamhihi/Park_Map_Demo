package com.example.parkmapofficial.main_ui.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkmapofficial.R;
import com.example.parkmapofficial.database.FirebaseDatabaseHelper;
import com.example.parkmapofficial.parkinglot.ClusterRenderer;
import com.example.parkmapofficial.parkinglot.ParkingLot;
import com.example.parkmapofficial.parkinglot.ParkingLotActivity;
import com.example.parkmapofficial.sharedpref.CustomSharedPreferences;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private MapsViewModel mapsViewModel;

    // Location
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private Marker currentMarker;

    // Additional widgets
    private FloatingActionButton gpsIcon;
    private AutocompleteSupportFragment autocompleteSupportFragment;

    // Cluster Item
    private ClusterManager<ParkingLot> mClusterManager;
    private ClusterRenderer mClusterRenderer;

    // Database
    private ArrayList<ParkingLot> mParkingLot = new ArrayList<>();

    // Fragment view
    private View root;

    // Save camera position
    private CameraPosition cameraPosition;

    // App Mode
    private CustomSharedPreferences appMode;

    // Values
    private boolean mLocationPermissionGranted = false;
    private String ERROR_TAG = this.getClass().getSimpleName();
    private String CAM_POS_KEY = "CAMERA_POSITION";
    private static final int DEFAULT_ZOOM = 16;
    private static final int SPECIAL_ZOOM = 18;
    private static final int MY_PERMISSIONS_REQUEST_FIND_LOCATION = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Use ViewModel to get data from Firebase
        mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);

        appMode = new CustomSharedPreferences(getActivity());

        // Get location
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Inflate fragment view
        root = inflater.inflate(R.layout.fragment_maps, container, false);

        // Setups for map and widgets
        setupMap();
        setupAutocomplete();
        setupGPS();

        // If change settings --> save camera position
        if (savedInstanceState != null && mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition((CameraPosition)
                            savedInstanceState.getParcelable(CAM_POS_KEY)));

        return root;
    }

    private void setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupAutocomplete() {
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS));
        autocompleteSupportFragment.setHint(getString(R.string.autocomplete_hint));
        EditText autocompleteEdit = (EditText)autocompleteSupportFragment.getView()
                .findViewById(R.id.places_autocomplete_search_input);
        autocompleteEdit.setTextAppearance(R.style.textCaption);
        if (appMode.getNightModeState())
            autocompleteEdit.setTextColor(getResources().getColor(R.color.whiteText));
        else
            autocompleteEdit.setTextColor(getResources().getColor(R.color.blackText));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                moveCamera(place.getLatLng(), SPECIAL_ZOOM);
                removeOldMarker();
                addNewMarker(place.getLatLng());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(ERROR_TAG, "Autocomplete error");
            }
        });
    }

    private void setupGPS() {
        // Add listener for gps icon
        gpsIcon = root.findViewById(R.id.gps);
        gpsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
                clearSearchInput();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(false);

        try {
            boolean getMapStyle = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            Objects.requireNonNull(getContext()), R.raw.style_json));
            if (!getMapStyle) {
                Log.e(ERROR_TAG, "Style parsing failed");
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e(ERROR_TAG, "Can't find style. Error: ", e);
        }

        setUpClusterManager();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
        setupParkingLotDatabase();
    }

    private void setUpClusterManager() {
        mClusterManager = new ClusterManager<ParkingLot>(getActivity(), mMap);
        mClusterRenderer = new ClusterRenderer(getContext(), mMap, mClusterManager);
        mClusterManager.setRenderer(mClusterRenderer);

        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ParkingLot>() {
            @Override
            public void onClusterItemInfoWindowClick(ParkingLot parkingLot) {
                // Navigate to ParkingLot Activity
                Intent intent = new Intent(getContext(), ParkingLotActivity.class);
                intent.putExtra("selectedLot", parkingLot);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
            }
        });
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext())
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FIND_LOCATION);
        } else {
            mLocationPermissionGranted = true;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        cameraPosition = mMap.getCameraPosition();
        outState.putParcelable(CAM_POS_KEY, cameraPosition);
        super.onSaveInstanceState(outState);
    }

    private void updateLocationUI() {
        if (mMap == null)
            return;
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: ", Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                moveCamera(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM);
                                removeOldMarker();
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    private void setupParkingLotDatabase() {
        new FirebaseDatabaseHelper().readParkingLots(new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataLoaded(ArrayList<ParkingLot> parkingLots, List<String> keys) {
                mParkingLot = parkingLots;
                mClusterManager.addItems(mParkingLot);
                mClusterManager.cluster();
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

    // ADDITIONAL METHODS

    // Animate camera
    private void moveCamera(LatLng latLng, int zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    // Response after requesting location permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FIND_LOCATION:
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true;
        }
        updateLocationUI();
    }

    private void clearSearchInput() {
        autocompleteSupportFragment.setText("");
    }

    private void removeOldMarker() {
        if (currentMarker != null)
            currentMarker.remove();
    }

    private void addNewMarker(LatLng latLng) {
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng));
    }
}
