package com.accprone.accprone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    ArrayList<ParseGeoPoint> geoPointsFrom = new ArrayList<>();
    ArrayList<ParseGeoPoint> getGeoPointsTo = new ArrayList<>();
    LocationListener locationListener;
    List<ParseGeoPoint> parseGeoPointsList = new ArrayList<>();
    ArrayList<PolylineOptions> polylineOptionsArrayList = new ArrayList<>();
    private GoogleMap mMap;
    Location nowLocation;
    int permissionCheck;
    public final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 99;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.i("Permissions","Granted");
                }else {
                    Log.i("Permissions","Denied");
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        geoPointsFrom.clear();
        getGeoPointsTo.clear();
        polylineOptionsArrayList.clear();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        DownloadGeoPoints downloadGeoPoints = new DownloadGeoPoints();
        downloadGeoPoints.execute();
        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck != 1){
            Log.i("Log.i",String.valueOf(permissionCheck));
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
        }
        nowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.i("nowLocation",String.valueOf(nowLocation));
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("nowLocation",String.valueOf(location));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();

        LatLng nowLatLng = new LatLng(nowLocation.getLatitude(), nowLocation.getLongitude());
       // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nowLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12f));
//        DownloadTask task = new DownloadTask();
//        task.execute();

        // Add a marker in Sydney and move the camera
//        Polyline line = mMap.addPolyline(new PolylineOptions()
//                .add(new LatLng(29.450457, 75.082295), new LatLng(29.465714, 75.069623))
//                .width(5)
//                .color(Color.RED));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                mMap.addMarker(new MarkerOptions().position(latLng).title("AccProneArea"));
//                Toast.makeText(getApplicationContext(),"to",Toast.LENGTH_LONG).show();
            }
        });


    }
    public class DownloadTask extends AsyncTask<String, Void,String>{

            @Override
            protected String doInBackground (String...strings){
            for(int j = 0;j<getGeoPointsTo.size();j++) {
                String result = "";
                PolylineOptions polylineOptions = new PolylineOptions();
                try {
                    URL url = new URL("https://roads.googleapis.com/v1/snapToRoads?path="+geoPointsFrom.get(j).getLatitude()+","+geoPointsFrom.get(j).getLongitude()+"|"+getGeoPointsTo.get(j).getLatitude()+","+getGeoPointsTo.get(j).getLongitude()+"&interpolate=true&key=AIzaSyBweORHwl8cQOrsJxAcd_F4dTtszdotrNE");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStreamReader reader = new InputStreamReader(httpURLConnection.getInputStream());
                    int data = reader.read();
                    while (data != -1) {
                        char current = (char) data;
                        result += current;
                        data = reader.read();
                    }

                    JSONObject jsonObject = new JSONObject(result);
                    String points = jsonObject.getString("snappedPoints");
                    JSONArray jsonArray = new JSONArray(points);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        points = jsonObject.getString("location");
                        jsonObject = new JSONObject(points);
                        String latitude = jsonObject.getString("latitude");
                        String longitude = jsonObject.getString("longitude");
                        LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                        polylineOptions.add(latLng);
                    }
                    polylineOptionsArrayList.add(polylineOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
            }

            @Override
            protected void onPostExecute (String s){
            super.onPostExecute(s);
            // Log.i("Results",result);
                try {
                    for (int k = 0; k < polylineOptionsArrayList.size(); k++) {
                        mMap.addPolyline(polylineOptionsArrayList.get(k).width(5).color(Color.RED).geodesic(false));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
             }

        }

    private void accProneBothPositionsSaveToParse(ParseGeoPoint from, ParseGeoPoint to) {
        ParseObject object = new ParseObject("accPronePositions");
        ParseGeoPoint geoPointFrom = new ParseGeoPoint(from.getLatitude(),from.getLongitude());
        object.put("LatLngFrom",geoPointFrom);
        ParseObject parseObject = new ParseObject("accPronePositionsTo");
        ParseGeoPoint geoPointTo = new ParseGeoPoint(to.getLatitude(),to.getLongitude());
        parseObject.put("LatLngTo",geoPointTo);
        parseObject.put("parent",object);
        parseObject.saveInBackground();
    }

    private void accProneBothPositionsSaveToParse(double v, double v1, double v2, double v3) {
        ParseObject object = new ParseObject("accPronePositions");
        ParseGeoPoint geoPointFrom = new ParseGeoPoint(v,v1);
        object.put("LatLngFrom",geoPointFrom);
        ParseObject parseObject = new ParseObject("accPronePositionsTo");
        ParseGeoPoint geoPointTo = new ParseGeoPoint(v2,v3);
        parseObject.put("LatLngTo",geoPointTo);
        parseObject.put("parent",object);
        parseObject.saveInBackground();
    }

    public class DownloadGeoPoints extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("accPronePositionsTo");

            query.findInBackground(new FindCallback<ParseObject>() {
                ParseObject parseObject;
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){

                        for (ParseObject object : objects) {
                            ParseGeoPoint parseGeoPoint = object.getParseGeoPoint("LatLngTo");
                            getGeoPointsTo.add(parseGeoPoint);
                            //   Log.i("LatLngTo",parseGeoPoint.toString());
                            object.getParseObject("parent")
                                    .fetchInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject object, ParseException e) {
                                            if(e == null) {
                                                ParseGeoPoint parseGeoPoint1 = object.getParseGeoPoint("LatLngFrom");
                                                geoPointsFrom.add(parseGeoPoint1);
                                                //   Log.i("LatLngFrom", parseGeoPoint1.toString());
                                            }
                                            else {
                                                e.printStackTrace();
                                            }
                                            Log.i("Points",String.valueOf(geoPointsFrom)+String.valueOf(getGeoPointsTo));
                                            if(geoPointsFrom.size() == getGeoPointsTo.size()){
                                                DownloadTask task = new DownloadTask();
                                                task.execute();
                                            }
                                        }
                                    });
                        }


                    }else {
                        e.printStackTrace();
                    }

                }

            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("Task","finished");
        }
    }
}
