package ss.exercisegps.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import ss.exercisegps.Constants.IntentExtraConstant;
import ss.exercisegps.DBModel.HistoryModel;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.SystemUtils;

public class ReportActivity extends AppCompatActivity {

    TextView bmr_base_calorie
            , bmr_tdee_calorie

            , running_burn_total_energy

            , selected_food_result_calorie

            , selected_food_percent

            , report_result_need;

    ProgressBar report_result_progress;

    Button save,delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        SystemUtils.setActivity(this);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        bmr_base_calorie = (TextView)findViewById(R.id.bmr_base_calorie);
        bmr_tdee_calorie = (TextView)findViewById(R.id.bmr_tdee_calorie);

        running_burn_total_energy = (TextView)findViewById(R.id.running_burn_total_energy);

        selected_food_result_calorie = (TextView)findViewById(R.id.selected_food_result_calorie);

        selected_food_percent = (TextView)findViewById(R.id.selected_food_percent);

        report_result_progress = (ProgressBar)findViewById(R.id.report_result_progress);

        report_result_need = (TextView) findViewById(R.id.report_result_need);

        save = (Button)findViewById(R.id.report_save);
        delete = (Button)findViewById(R.id.report_delete);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ReportActivity.this)
                        .setTitle(getString(R.string.save_report))
                        .setMessage(getString(R.string.confirm_save_report))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                Float bmr = 0F,tdee = 0F
                                        , burn_energy = 0F
                                        , eat_energy = 0F;
                                Bundle bundle = getIntent().getExtras();
                                try {
                                    bmr = Float.parseFloat(bundle.getString(IntentExtraConstant.bmr_base_calorie));
                                    tdee = Float.parseFloat(bundle.getString(IntentExtraConstant.bmr_tdee_calorie));
                                    burn_energy = Float.parseFloat(bundle.getString(IntentExtraConstant.running_burn_total_energy));
                                    eat_energy = Float.parseFloat(bundle.getString(IntentExtraConstant.selected_food_result_calorie));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                HistoryModel historyModel = new HistoryModel();
                                historyModel.setBmr(bmr);
                                historyModel.setTdee(tdee);
                                historyModel.setTotal_energy(burn_energy);
                                historyModel.setResult_calorie(eat_energy);
                                SystemUtils.getSQLiteManager().addDataHistory(historyModel);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ReportActivity.this)
                        .setTitle(getString(R.string.delete_report))
                        .setMessage(getString(R.string.confirm_delete_report))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle bundle = getIntent().getExtras();
                                if(SystemUtils.getSQLiteManager().deleteDataHistory(bundle.getInt(IntentExtraConstant.report_id))) {
                                    SystemUtils.showLongToast("ลบรายงานสำเร็จ");
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra(IntentExtraConstant.report_date, bundle.getLong(IntentExtraConstant.report_date));
                                    setResult(Activity.RESULT_OK, resultIntent);
                                    finish();
                                } else {
                                    SystemUtils.showLongToast("ลบรายงานไม่สำเร็จ");
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        try {
            Bundle bundle = getIntent().getExtras();
            Float tdee = 0F
                    , burn_energy = 0F
                    , eat_energy = 0F;
            try {
                tdee = Float.parseFloat(bundle.getString(IntentExtraConstant.bmr_tdee_calorie));
                burn_energy = Float.parseFloat(bundle.getString(IntentExtraConstant.running_burn_total_energy));
                eat_energy = Float.parseFloat(bundle.getString(IntentExtraConstant.selected_food_result_calorie));
            } catch (Exception e) {
                e.printStackTrace();
            }

            setTitle(bundle.getString(IntentExtraConstant.title, getString(R.string.menu_report)));

            bmr_base_calorie.setText(bundle.getString(IntentExtraConstant.bmr_base_calorie));
            bmr_tdee_calorie.setText(bundle.getString(IntentExtraConstant.bmr_tdee_calorie));

            running_burn_total_energy.setText(bundle.getString(IntentExtraConstant.running_burn_total_energy));

            selected_food_result_calorie.setText(bundle.getString(IntentExtraConstant.selected_food_result_calorie));


            Float result_energy = tdee+burn_energy;

            String selected_food_percent_str = String.format(Locale.ROOT,"%.2f",(eat_energy/tdee)*100F)+ " %";


            String result_need_str = "";
            if(eat_energy<result_energy) {
                result_need_str = getString(R.string.need_energy)+ " " +
                        (result_energy - eat_energy) + " " + getString(R.string.kilo_calorie);
            } else {
                result_need_str = getString(R.string.enough_energy);

                if(eat_energy>result_energy) {

                    result_need_str += "\n"+getString(R.string.over_energy)+ " " +
                            (eat_energy - result_energy) + " " + getString(R.string.kilo_calorie);
                }
            }
            report_result_need.setText(result_need_str);

            selected_food_percent.setText(selected_food_percent_str);

            report_result_progress.setMax(tdee.intValue());
            report_result_progress.setProgress(eat_energy.intValue());


            if(bundle.getInt(IntentExtraConstant.report_id,-1)!=-1) {
                save.setVisibility(View.GONE);
                delete.setVisibility(View.VISIBLE);
            }
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
}
