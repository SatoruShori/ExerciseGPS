package ss.exercisegps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ss.exercisegps.Activities.BRMActivity;
import ss.exercisegps.Activities.HistoryActivity;
import ss.exercisegps.Activities.LocationActivity;
import ss.exercisegps.Activities.ReportActivity;
import ss.exercisegps.Activities.RunningBurnActivity;
import ss.exercisegps.Activities.SelectedFoodActivity;
import ss.exercisegps.Constants.IntentExtraConstant;
import ss.exercisegps.Constants.ShareConstants;
import ss.exercisegps.Utillities.SystemUtils;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    int[] menu_id = {
            R.id.menu_location
            , R.id.menu_bmr
            , R.id.menu_running_burn
            , R.id.menu_selected_food_menu
            , R.id.menu_report
            , R.id.menu_history
    };

    Class[] newActivityClass = {
            LocationActivity.class
            , BRMActivity.class
            , RunningBurnActivity.class
            , SelectedFoodActivity.class
            , ReportActivity.class
            , HistoryActivity.class
    };

    String currentDate = "";

    private static final int REQUEST_PLACE_PICKER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemUtils.setActivity(this);
        currentDate = getDate();
        setTitle(getTitle() + " ("+currentDate+")");
        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        setView();
    }

    private void setView() {
        for(short i=0;i<menu_id.length;i++) {
            final short finalI = i;
            LinearLayout linearLayout = (LinearLayout) findViewById(menu_id[i]);
            try {
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(finalI==0) {
//                            onPickButtonClick(v);
//                        } else {
                            Intent intent = new Intent(MainActivity.this,newActivityClass[finalI]);
                            if(finalI==4) {
                                intent
                                        .putExtra(IntentExtraConstant.title, getString(R.string.menu_report)+ " (วันนี้ "+currentDate+")")
                                        .putExtra(IntentExtraConstant.bmr_base_calorie, sharedPreferences.getString(ShareConstants.BMR_BASE_CALORIE,""))
                                        .putExtra(IntentExtraConstant.bmr_tdee_calorie, sharedPreferences.getString(ShareConstants.BMR_TOTAL_CALORIE,""))

                                        .putExtra(IntentExtraConstant.running_burn_total_energy, sharedPreferences.getString(ShareConstants.RUNNING_BURN_TOTAL_ENERGY,""))

                                        .putExtra(IntentExtraConstant.selected_food_result_calorie, ""+sharedPreferences.getInt(ShareConstants.SELECTED_FOOD_CALORIE,0));
                            }
                            startActivity(intent);
//                        }
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public String getDate() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMMM-yyyy", new Locale("th", "TH"));
        return df.format(c.getTime());

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

            // The user has selected a place. Extract the name and address.
            Place place = PlacePicker.getPlace(this, data);
            LatLng latLng = place.getLatLng();
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
