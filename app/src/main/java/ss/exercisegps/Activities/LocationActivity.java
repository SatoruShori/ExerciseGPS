package ss.exercisegps.Activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ss.exercisegps.Connector.NetworkConnection;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.LoadingDialog;
import ss.exercisegps.Utillities.SystemUtils;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    SharedPreferences sharedPreferences;
    GoogleMap mGoogleMap;

    private DrawerLayout mDrawerLayout;
//    private ActionBarDrawerToggle mDrawerToggle;
    ImageView location_slide_image;
    ImageButton location_search
        , location_slide_direction
        , location_slide_show;

    LinearLayout location_slide_content;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private String map_api_key;

    private static final int REQUEST_PLACE_PICKER = 1;

    private static final String TAG = "LocationActivity";

    Marker selectedMarker;
//    List<Map<String,Double>> marker_latLng = new ArrayList<>();
//    List<Map<String,String>> marker_data = new ArrayList<>();

    List<Map> locationList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();


    String keyName = "name"
            , keyLatitude = "latitude"
            , keyLongitude = "longitude"
            , keyOpenTime = "open_time"
            , keyImage = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        SystemUtils.setActivity(this);
        sharedPreferences = SystemUtils.getSharedPreferences(getString(R.string.preference_file_key));
        map_api_key = getString(R.string.google_maps_key);

        location_slide_content = (LinearLayout)findViewById(R.id.location_slide_content);
        location_slide_image = (ImageView)findViewById(R.id.location_slide_image);
        location_search = (ImageButton)findViewById(R.id.location_search);
        location_slide_direction = (ImageButton)findViewById(R.id.location_slide_direction);
        location_slide_show = (ImageButton)findViewById(R.id.location_slide_show);
        setBind();

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name,R.string.app_name){
//            @Override
//            public void onDrawerClosed(View view) {
//                invalidateOptionsMenu();
//            }
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                invalidateOptionsMenu();
//            }
//        };
//        mDrawerLayout.addDrawerListener(mDrawerToggle);

        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.location_fragment_map_container, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
    }

    private void setBind() {
        location_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickButtonClick(v);
//                if(mLastLocation!=null) {
//                    String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
//                            "?location=" +
//                            13.7849348 +
//                            "," +
//                            100.6261148 +
//                            "&radius=5000" +
//                            "&type=sport|gym" +
//                            "&keyword=sport|fitness" +
//                            "&language=th" +
//                            "&key="+map_api_key;
//                    NetworkConnection networkConnection = NetworkConnection.getInstance();
//                    networkConnection.postRequest(url, new NetworkConnection.ResultData<Map<String,Object>>() {
//                        @Override
//                        public void getData(Map<String,Object> data) {
//                            try {
//                                String status = (String)data.get("status");
//                                if("OK".equals(status)) {
//                                    List<Map<String,Object>> listData = (List<Map<String,Object>> )data.get("results");
//                                    for(Map<String,Object> mapData : listData) {
//                                        try {
//                                            Map<String, Double> mapLocation = (Map<String, Double>)((Map<String,Object>)mapData.get("geometry")).get("location");
//                                            LatLng latLng = new LatLng(mapLocation.get("lat"),mapLocation.get("lng"));
//                                            String tw_name = (String)mapData.get("tw_name");
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(IOException e) {
//
//                        }
//                    });
//                }
            }
        });

        location_slide_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMarker!=null) {

                    Map map = locationList.get((int)selectedMarker.getTag());
                    String name = (String)map.get(keyName);
                    LatLng latLng = new LatLng((Double)map.get(keyLatitude), (Double)map.get(keyLongitude));
                    String image = (String)map.get(keyImage);

                    location_slide_image.setImageResource(R.drawable.ic_photos);
                    new AsyncTask<String,Void,Bitmap>() {

                        @Override
                        protected Bitmap doInBackground(String... params) {
                            try {
                                int drawableResourceId = getResources().getIdentifier(params[0], "drawable", getPackageName());
                                return SystemUtils.decodeSampledBitmapFromResource(drawableResourceId, SystemUtils.dp2px(240), SystemUtils.dp2px(160));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            if(result!=null) {
                                location_slide_image.setImageBitmap(result);
                            }
                        }
                    }.execute(image);

                    location_slide_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map map = locationList.get((int)selectedMarker.getTag());
                            String image = (String)map.get(keyImage);
                            Intent intent = new Intent(LocationActivity.this, ImageActivity.class);
                            intent.putExtra("imageName", image);
                            startActivity(intent);
                        }
                    });
                    location_slide_content.removeAllViews();
                    TextView textView = new TextView(SystemUtils.getActivity());
                    SystemUtils.addView(location_slide_content,textView);
                    textView.setText(Html.fromHtml((String)map.get(keyOpenTime)));
                    openDrawer();
                }
            }
        });

        location_slide_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedMarker!=null) {
                    Marker marker = selectedMarker;
                    LatLng latLng = marker.getPosition();
                    String uri = "google.navigation:q=" +
                            latLng.latitude +
                            "," +
                            latLng.longitude +
                            "&mode=w" +
                            "&avoid=t";
                    Uri gmmIntentUri = Uri.parse(uri);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        });
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    public void openDrawer() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        try {
            UiSettings uiSettings = googleMap.getUiSettings();
            this.mGoogleMap.setMyLocationEnabled(true);
            uiSettings.setZoomControlsEnabled(true);
            uiSettings.setCompassEnabled(true);
            uiSettings.setRotateGesturesEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        try {
            LoadingDialog.show();
            new AsyncTask<Void,Void,String>() {

                @Override
                protected String doInBackground(Void... params) {
                    return readLocationData();
                }

                @Override
                protected void onPostExecute(String result) {
                    locationList = SystemUtils.getGson().fromJson(result, new TypeToken<List<Map>>(){}.getType());
                    System.out.println(SystemUtils.getGson().toJson(locationList));
                    for(int index = 0; index<locationList.size();index++) {
                        Map map = locationList.get(index);
                        addMarker(index, map);
                    }
                    LoadingDialog.close();

                }
            }.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    selectedMarker = marker;
                    location_slide_show.setVisibility(View.VISIBLE);
                    return false;
                }
            });
            this.mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    selectedMarker = null;
                    location_slide_show.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

//    @Override
//    public void onBackPressed() {
//        if(location_slide_show.getVisibility()==View.VISIBLE) {
//            location_slide_show.setVisibility(View.GONE);
//            mDrawerLayout.closeDrawers();
//            selectedMarker = null;
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    protected void onStart() {
        try {
            if(mGoogleApiClient!=null) mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        try {
            if(mGoogleApiClient!=null) mGoogleApiClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        try {
            if(mGoogleApiClient!=null) mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        try {
            if(mGoogleApiClient!=null) mGoogleApiClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            mLastLocation = location;
        }
//        if(location!=null) {
//            LatLng latLng = new LatLng(location.getLatitude()
//                    , location.getLongitude());
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mGoogleMap.getCameraPosition().zoom);
//            mGoogleMap.animateCamera(cameraUpdate);
//        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

//            if (mLastLocation != null) {
//                LatLng latLng = new LatLng(mLastLocation.getLatitude()
//                        , mLastLocation.getLongitude());
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
//                mGoogleMap.animateCamera(cameraUpdate);
//            }

            LatLng latLng = new LatLng(16.1903, 103.2862);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
            mGoogleMap.animateCamera(cameraUpdate);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    public void onPickButtonClick(View v) {
        // Construct an intent for the place picker
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the intent by requesting a result,
            // identified by a request code.
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

        } catch (GooglePlayServicesRepairableException e) {
            // ...
        } catch (GooglePlayServicesNotAvailableException e) {
            // ...
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the tw_name and address.
            Place place = PlacePicker.getPlace(this, data);

//            final CharSequence tw_name = place.getName();
//            final CharSequence address = place.getAddress();
//            String attributions = PlacePicker.getAttributions(data);
//            if (attributions == null) {
//                attributions = "";
//            }
            if(selectedMarker!=null) {
                selectedMarker.remove();
            }
            selectedMarker = mGoogleMap.addMarker(new MarkerOptions()
            .position(place.getLatLng())
            .title(place.getName().toString())
            .snippet(place.getAddress().toString()));
            if(mLastLocation!=null) {

            }
            LatLng directionLatLng = place.getLatLng();
            String url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin="+mLastLocation.getLatitude()+ ","+mLastLocation.getLongitude() +
                    "&destination="+directionLatLng.latitude+ ","+directionLatLng.longitude +
                    "&mode=walking" +
                    "&avoid=tolls" +
                    "&language=th" +
                    "&key="+map_api_key;
            NetworkConnection networkConnection = NetworkConnection.getInstance();
            networkConnection.postRequest(url, new NetworkConnection.ResultData<Map<String,Object>>() {
                @Override
                public void getData(Map<String,Object> data) {
                    String status = (String)data.get("status");
                    if("OK".equals(status)) {
                        List<Map<String,Object>> routes = (List<Map<String,Object>>)data.get("routes");
                        if(routes.size()>0) {
                            final String overview_polyline = ((Map<String,String>)routes.get(0).get("overview_polyline")).get("points");
                            final List<LatLng> latLngList = PolyUtil.decode(overview_polyline);
                            SystemUtils.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    List<LatLng> latLngList = PolyUtil.decode(overview_polyline);
                                    mGoogleMap.addPolyline(new PolylineOptions()
                                        .addAll(latLngList));
                                }
                            });
                        }
                        SystemUtils.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SystemUtils.showLongToast("OK");
                            }
                        });
                    }
                }

                @Override
                public void onFailure(IOException e) {

                }
            });

//            mViewName.setText(tw_name);
//            mViewAddress.setText(address);
//            mViewAttributions.setText(Html.fromHtml(attributions));

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String readLocationData() {
        InputStream is = getResources().openRawResource(R.raw.location);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String json = writer.toString();
        System.out.println(json);
        return json;
    }

    private void addMarker(int index, Map map) {
        if(mGoogleMap !=null) {
            Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                    .title((String)map.get(keyName))
                    .position(new LatLng((Double)map.get(keyLatitude), (Double)map.get(keyLongitude)))
            );
            marker.setTag(index);
            markerList.add(marker);
        }
    }
}
