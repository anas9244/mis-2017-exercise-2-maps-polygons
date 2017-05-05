package com.example.anas.exercise02;

import android.Manifest;

import android.app.LauncherActivity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    SharedPreferences sharedPreferences;
    int locationCount = 0;
    private GoogleMap mMap;
    EditText editText;
    Button button;

    Polygon shape=null;
    List <LatLng> vertices;
    LatLng centerPoint=null;
    boolean isCenter=false;
    Marker centerMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editText = (EditText) findViewById(R.id.editText);

        vertices = new ArrayList<>();

        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shape!=null)
                {

                    button.setText("Start Polygon");
                    shape.remove();
                    shape=null;
                    mMap.clear();
                    vertices.clear();
                    locationCount=0;


                }
                else
                    {
                        if (vertices.size() > 2)
                        {
                         drawPoly();
                         button.setText("END Polygon");


                        Double area = Math.round(SphericalUtil.computeArea(vertices) * 100D) / 100D;

                        LatLngBounds.Builder centroidBuilder = new LatLngBounds.Builder();

                        for (LatLng vertex : vertices) {

                            centroidBuilder.include(vertex);

                        }
                        centerPoint = centroidBuilder.build().getCenter();

                                centerMarker= mMap.addMarker(new MarkerOptions()
                                .position(centerPoint)
                                .title(Double.toString(area) + " " + "m²")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker5)));


                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Please add more than 2 markers", Toast.LENGTH_SHORT).show();

                        }
                }
            }
        });



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);


        // Opening the sharedPreferences object
        sharedPreferences = getSharedPreferences("location", 0);

        // Getting number of locations already stored
        locationCount = sharedPreferences.getInt("locationCount", 0);

        // Getting stored zoom level if exists else return 0
        String zoom = sharedPreferences.getString("zoom", "0");

        if(locationCount!=0){

            String lat = "";
            String lng = "";
            String title= "";

            // Iterating through all the locations stored
            for(int i=0;i<locationCount;i++){

                // Getting the latitude of the i-th location
                lat = sharedPreferences.getString("lat"+i,"0");

                // Getting the longitude of the i-th location
                lng = sharedPreferences.getString("lng"+i,"0");

                // Getting the title of the i-th location
                title=sharedPreferences.getString("title"+i,"0");

                // Drawing marker on the map
                drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)),title);

                vertices.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            }

            // Moving CameraPosition to last clicked position
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))));

            // Setting the zoom level in the map on last position is clicked
            mMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom)));


        }




        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                locationCount++;


                drawMarker(latLng,editText.getText().toString());

                vertices.add(latLng);

                /** Opening the editor object to write data to sharedPreferences */
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Storing the latitude for the i-th location
                editor.putString("lat"+ Integer.toString((locationCount-1)), Double.toString(latLng.latitude));

                // Storing the longitude for the i-th location
                editor.putString("lng"+ Integer.toString((locationCount-1)), Double.toString(latLng.longitude));

                // Storing the title for the i-th location
                editor.putString("title"+ Integer.toString((locationCount-1)), editText.getText().toString());


                // Storing the count of locations or marker count
                editor.putInt("locationCount", locationCount);

                /** Storing the zoom level to the shared preferences */
                editor.putString("zoom", Float.toString(mMap.getCameraPosition().zoom));

                /** Saving the values stored in the shared preferences */
                editor.commit();




                Toast.makeText(getBaseContext(), "Marker is added to the Map", Toast.LENGTH_SHORT).show();

            }
        });



    }

    private void drawMarker(LatLng point, String title){
    // Creating an instance of MarkerOptions
        MarkerOptions markerOptions = new MarkerOptions();

    // Setting latitude and longitude for the marker
        markerOptions.position(point);
        markerOptions.title(title);
    // Adding marker on the Google Map
        mMap.addMarker(markerOptions);
    }


    private void drawPoly()
    {

        String lat="";
        String lng="";

        PolygonOptions polyOpt= new PolygonOptions()
                .fillColor(0x200000ff)
                .strokeWidth(5)
                .strokeColor(Color.BLUE);

       if (shape==null)
       {
        for (int i=0;i<locationCount;i++)
        {
            // Getting the latitude of the i-th location
            lat = sharedPreferences.getString("lat"+i,"0");

            // Getting the longitude of the i-th location
            lng = sharedPreferences.getString("lng"+i,"0");
            polyOpt.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
        }

        shape= mMap.addPolygon(polyOpt);
       }
    }




}

