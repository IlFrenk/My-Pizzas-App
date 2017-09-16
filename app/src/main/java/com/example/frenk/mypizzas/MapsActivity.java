package com.example.frenk.mypizzas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    protected LocationManager locationManager;
    //protected LocationListener myPosition;
    private GoogleMap mMap;
    boolean firstTime = true;
    float mapZoom = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, this); //register the activity to be updated periodically (provider, minTime, minDistance, Intent)
        // Instantiate the RequestQueue.
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
        //mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        Marker myPos = mMap.addMarker(new MarkerOptions()
            .position(new LatLng(location.getLatitude(), location.getLongitude()))
            .title("La tua posizione")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.down_finger)));
        myPos.showInfoWindow();
        findPizzerias(location);
    }

    void findPizzerias(final Location e){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+ e.getLatitude() +","+e.getLongitude()+"&radius=500&type=restaurant&keyword=pizza&key=AIzaSyAf_F1XcHp_zJYCR_jDHYnGhEi0ATQiCpI\n";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject origObj = new JSONObject(response);
                            JSONArray myArr = origObj.getJSONArray("results");
                            for(int i = 0; i<myArr.length(); i++){
                                JSONObject jsO = myArr.getJSONObject(i);
                                JSONObject geometry = jsO.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                Double lat = location.getDouble("lat");
                                Double lng = location.getDouble("lng");
                                //Log.d(jsO.getString("name"),String.valueOf(lat));
                                final LatLng currPoint = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions()
                                        .position(currPoint)
                                        .title(jsO.getString("name"))
                                        .snippet("Get Directions")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pizza)));


                                mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                                    @Override
                                    public void onInfoWindowClick(Marker marker) {
                                        String uriTemp = "http://maps.google.com/?daddr=" + currPoint.latitude + "," + currPoint.longitude;
                                        Uri uri = Uri.parse(uriTemp);
                                        showMap(uri);
                                    }
                                });

                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Non","Funziona");
            }
        });
    // Add the request to the RequestQueue.
        queue.add(stringRequest);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(e.getLatitude(), e.getLongitude()), mapZoom));

        if(firstTime) {
            mapZoom += 5;
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
            mMap.animateCamera(CameraUpdateFactory.zoomTo(mapZoom), 3000, null);
            firstTime = false;
        }
    }


    public void showMap(Uri geoLocation) {
        Intent i = new Intent(Intent.ACTION_VIEW, geoLocation);
        //i.setData(geoLocation);
        i.setPackage("com.google.android.apps.maps");
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivity(i);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*@Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Voidì, tanta robba",
                Toast.LENGTH_SHORT).show();
    }*/


}
