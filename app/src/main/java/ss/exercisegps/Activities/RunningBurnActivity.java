package ss.exercisegps.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ss.exercisegps.Constants.ShareConstants;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.CountUpTimer;
import ss.exercisegps.Utillities.SystemUtils;

public class RunningBurnActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    LayoutInflater inflater;
    SharedPreferences sharedPreferences;

    CountUpTimer countUpTimer;

    Button bt_start_stop
            , bt_reset
            , bt_save;

    LinearLayout running_burn_content;

    TextView tw_timer_s
            , tw_timer_m
            , tw_timer_h
            ,  tw_running_burn_total_time
            , tw_running_burn_total_distance
            , tw_running_burn_total_energy
            , tw_distance
            , tw_running_burn_gps_status
            , tw_running_burn_latitude
            , tw_running_burn_longitude;

    Boolean timer_status = false;
    String str_start
            , str_stop;

    Long timer, total_time = 0L;
    Double total_distance = 0D, total_energy = 0D;

    Integer calorie_result = 0;

    Double distance_gps_total = 0D;

    List<Map<String,Object>> list_data = new ArrayList<>();
    List<View> list_content_view = new ArrayList<>();

    Gson gson;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    Location mLastLocation;

    int REQUEST_LOCATION = 199
            , PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 200;

    boolean gps_status = false;

    IntentFilter gpsFilter = new IntentFilter();
    BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                CheckStatusGPS(context);
            }
        }
    };

    public class TimerItem {

        View view;
        TextView tx_time,tx_distance,tx_calorie;
        Button delete;

        TimerItem(Long times, Double distance, Double energy) {
            try {
                int numberIndex = running_burn_content.getChildCount();

                view = inflater.inflate(R.layout.running_burn_item, null, false);
                view.setTag(times);
                view.setBackgroundColor(numberIndex%2==0?Color.LTGRAY:Color.WHITE);
                tx_time = (TextView) view.findViewById(R.id.running_burn_item_time);
                tx_distance = (TextView) view.findViewById(R.id.running_burn_item_distance);
                tx_calorie = (TextView) view.findViewById(R.id.running_burn_item_calorie);
                delete = (Button) view.findViewById(R.id.running_burn_item_delete);
                tx_time.setText(getTextTime(times));
                tx_distance.setText(distance+"");
                tx_calorie.setText(energy+"");

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Long times = (Long)view.getTag();
                            Double distance = Double.parseDouble(tx_distance.getText().toString());
                            Double calorie = Double.parseDouble(tx_calorie.getText().toString());
                            int index = running_burn_content.indexOfChild(view);
                            list_data.remove(index);
                            running_burn_content.removeViewAt(index);
                            list_content_view.remove(index);

                            total_time -= times;
                            total_distance -= distance;
                            total_energy -= calorie;
                            setTotalText();
                            resetItemBackgroundColor();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                total_time += times;
                total_distance += distance;
                total_energy += energy;
                setTotalText();
                running_burn_content.addView(view);
                list_content_view.add(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setTotalText() {
            tw_running_burn_total_time.setText(getTextTime(total_time));
            tw_running_burn_total_distance.setText(""+total_distance);
            tw_running_burn_total_energy.setText(""+total_energy);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_burn);
        SystemUtils.setActivity(this);
        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        inflater = (LayoutInflater) SystemUtils.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gson = SystemUtils.getGson();
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        str_start = getString(R.string.start);
        str_stop = getString(R.string.stop);

        tw_running_burn_gps_status = (TextView)findViewById(R.id.running_burn_gps_status);
        tw_running_burn_latitude = (TextView)findViewById(R.id.running_burn_latitude);
        tw_running_burn_longitude = (TextView)findViewById(R.id.running_burn_longitude);

        tw_timer_s = (TextView) findViewById(R.id.running_burn_timer_second);
        tw_timer_m = (TextView)findViewById(R.id.running_burn_timer_minute);
        tw_timer_h = (TextView)findViewById(R.id.running_burn_timer_hour);
        bt_start_stop = (Button)findViewById(R.id.running_burn_start_stop);

        bt_reset = (Button)findViewById(R.id.running_burn_reset);
        tw_distance = (TextView)findViewById(R.id.running_burn_item_distance);
        bt_save = (Button)findViewById(R.id.running_burn_save);

        tw_running_burn_total_time = (TextView)findViewById(R.id.running_burn_total_time);
        tw_running_burn_total_distance = (TextView)findViewById(R.id.running_burn_total_distance);
        tw_running_burn_total_energy = (TextView)findViewById(R.id.running_burn_total_energy);

        running_burn_content = (LinearLayout)findViewById(R.id.running_burn_content);

        countUpTimer = new CountUpTimer(10) {
            @Override
            public void onTick(long elapsedTime) {
                timer = elapsedTime;
                double second = (elapsedTime/1000D)%60D;
                long min = (TimeUnit.MILLISECONDS.toMinutes(timer))%60
                        , hour = TimeUnit.MILLISECONDS.toHours(timer);
                float floatValue = Float.parseFloat(second+"");
                String sec = String.format(Locale.ROOT,"%.2f",floatValue);
                tw_timer_s.setText(sec);
                tw_timer_m.setText(min+ "");
                tw_timer_h.setText(hour+ "");

            }
        };
        bt_start_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleTimer();
            }
        });
        bt_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countUpTimer.reset();
                timer = 0L;
                bt_save.setEnabled(false);
                tw_timer_s.setText("0");
                tw_timer_m.setText("0");
                tw_timer_h.setText("0");
                distance_gps_total = 0D;
                tw_distance.setText("0");
            }
        });
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double distance = 0D;
                try {
                    distance = Double.parseDouble(tw_distance.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                timer = getTimerFromEditText();
                System.out.println("Timer = "+timer);
                Double weight = Double.parseDouble(sharedPreferences.getString(ShareConstants.BMR_WEIGHT,"0"));
                if(weight<=0) {
                    SystemUtils.showLongToast("โปรดคำนวน BMR ก่อนคำนวนการวิ่ง...");
                    return;
                }
                Double energy = equation(weight, distance, timer);;
                new TimerItem(timer, distance, energy);
                Map<String, Object> map = new HashMap<>();
                map.put("time", timer);
                map.put("distance",distance);
                map.put("energy", energy);
                System.out.println(map);
                if(list_data==null) {
                    list_data = new ArrayList<>();
                }
                list_data.add(map);
            }
        });

        try {
            list_data = gson.fromJson(
                    sharedPreferences.getString(ShareConstants.RUNNING_BURN_DATA_LIST,"")
                    , new TypeToken<List<Map<String, Object>>>(){}.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(list_data!=null) {
            for(Map<String,Object> map : list_data) {
                Long time = ((Double)map.get("time")).longValue();
                Double distance = (Double)map.get("distance")
                        , energy = (Double)map.get("energy");
                new TimerItem(time, distance, energy);
            }
        }

        CheckStatusGPS(this);
        buildGoogleApiClient();
        if(gpsFilter!=null && gpsReceiver!=null) {
            gpsFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            registerReceiver(gpsReceiver, gpsFilter);
        }
        openRequestGPS();
    }

    void CheckStatusGPS(Context context) {
        try {
            LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
            if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                gps_status = true;
                tw_running_burn_gps_status.setText(getString(R.string.gps_on));
                tw_running_burn_gps_status.setBackgroundResource(R.color.color_green);
            }
            else {
                gps_status = false;
                tw_running_burn_gps_status.setText(getString(R.string.gps_off));
                tw_running_burn_gps_status.setBackgroundResource(R.color.color_red);
                bt_start_stop.setEnabled(false);
                if(timer_status) {
                    toggleTimer();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void toggleTimer() {
        if(!timer_status) {
            countUpTimer.start();
            bt_start_stop.setText(str_stop);
        } else {
            countUpTimer.stop();
            bt_start_stop.setText(str_start);
        }
        bt_reset.setEnabled(timer_status);
        bt_save.setEnabled(timer_status);
        timer_status = !timer_status;
    }

    String getTextTime(Long times) {
        double second = (times/1000D)%60D;
        long min = TimeUnit.MILLISECONDS.toMinutes(times)%60
                , hour = TimeUnit.MILLISECONDS.toHours(times);
        String sec = String.format(Locale.ROOT,"%.2f",Float.parseFloat(second+""));
        return hour + " : " + min + " : " + sec;
    }

    Long getTimerFromEditText() {
        String str_sec = tw_timer_s.getText().toString()
                , str_min = tw_timer_m.getText().toString()
                , str_hour = tw_timer_h.getText().toString();
        long times = 0;
        try {
            times += Integer.parseInt(str_hour)*60*60*1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            times += Integer.parseInt(str_min)*60*1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            times += Double.parseDouble(str_sec)*1000;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return times;
    }

    private void resetItemBackgroundColor() {
        for(int i=0; i<list_content_view.size();i++) {
            View view = list_content_view.get(i);
            view.setBackgroundColor(i%2==0?Color.LTGRAY:Color.WHITE);
        }
    }

    private Double equation(Double weight, Double distance, long times) {
        float sec = times/1000F;
        System.out.println("weight : "+weight+
        "\ndistance : "+distance+
        "\ntimes : "+times);
        Double energy = weight*distance*0.000575F*sec;

        String en = String.format(Locale.ROOT,"%.2f",energy);
        return Double.parseDouble(en);
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

    @Override
    protected void onStart() {
        try {
            if(googleApiClient !=null) googleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        try {
            if(googleApiClient !=null) googleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(gpsFilter!=null && gpsReceiver!=null) {
            gpsFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
            registerReceiver(gpsReceiver, gpsFilter);
        }
        super.onResume();
    }

    @Override
    public void onStop () {
        sharedPreferences.edit()
                .putString(ShareConstants.RUNNING_BURN_DATA_LIST, gson.toJson(list_data))
                .putLong(ShareConstants.RUNNING_BURN_TOTAL_TIME, total_time)
                .putString(ShareConstants.RUNNING_BURN_TOTAL_TIME_STR, getTextTime(total_time))
                .putString(ShareConstants.RUNNING_BURN_TOTAL_DISTANCE, ""+total_distance)
                .putString(ShareConstants.RUNNING_BURN_TOTAL_ENERGY, ""+total_energy)
                .apply();
        try {
            if(googleApiClient !=null) googleApiClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(gpsReceiver!=null) {
                unregisterReceiver(gpsReceiver);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if(googleApiClient !=null) googleApiClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(gpsReceiver!=null) {
                unregisterReceiver(gpsReceiver);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3 * 1000);
        locationRequest.setFastestInterval(1000);

//        openRequestGPS();
    }

    private void openRequestGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                Status status = result.getStatus();
//                LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS: {

                        break;
                    }

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        try {
                            status.startResolutionForResult(RunningBurnActivity.this, REQUEST_LOCATION);

                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    }

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE: {

                        break;
                    }
                }
            }
        });
    }

    void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            } else {

                // No explanation needed, we can request the permission.
                // PERMISSION_REQUEST_ACCESS_FINE_LOCATION can be any unique int
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_LOCATION) {
            if(resultCode==Activity.RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
                                           String permissions[], @NonNull int[] grantResults) {
        if (requestCode==PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                requestLocation();
            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
                // If request is cancelled, the result arrays are empty.
        }

            // other 'case' lines to check for other
            // permissions this app might request
    }

    void requestLocation() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    googleApiClient);
            tw_running_burn_latitude.setText(""+mLastLocation.getLatitude());
            tw_running_burn_longitude.setText(""+mLastLocation.getLongitude());

            bt_start_stop.setEnabled(true);

        } catch (SecurityException e) {
            e.printStackTrace();
            checkPermission();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            tw_running_burn_latitude.setText(""+location.getLatitude());
            tw_running_burn_longitude.setText(""+location.getLongitude());
            if(!bt_start_stop.isEnabled()) {
                bt_start_stop.setEnabled(true);
            }
            if(mLastLocation !=null && timer_status) {
                float[] result = new float[1];
                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude()
                , location.getLatitude(), location.getLongitude(), result);
                distance_gps_total += result[0]/1000D;

                float floatValue = Float.parseFloat(distance_gps_total+"");
                String str_distance_gps_total = String.format(Locale.ROOT,"%.3f",floatValue);
                tw_distance.setText(str_distance_gps_total);
            }
            mLastLocation = location;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }
}
