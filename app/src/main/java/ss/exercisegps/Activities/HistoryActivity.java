package ss.exercisegps.Activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ss.exercisegps.Connector.SQLiteManager;
import ss.exercisegps.Constants.IntentExtraConstant;
import ss.exercisegps.DBModel.HistoryModel;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.LoadingDialog;
import ss.exercisegps.Utillities.SystemUtils;

public class HistoryActivity extends AppCompatActivity {

//    CalendarView calendarView;

    Button reset
//            , select_day
            , select_month
            , select_year;

//    Calendar calendar;
    CaldroidFragment caldroidFragment;
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", new Locale("th", "TH"));
    SimpleDateFormat hdf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", new Locale("th", "TH"));


    AlertDialog alertDialog;

    int DELETE_REPORT_RETURN = 445;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        SystemUtils.setActivity(this);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

//        calendarView = (CalendarView)findViewById(R.id.calendar);

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        reset = (Button)findViewById(R.id.history_reset);
//        select_day = (Button)findViewById(R.id.history_select_day);
        select_month = (Button)findViewById(R.id.history_select_month);
        select_year = (Button)findViewById(R.id.history_select_year);
//        calendar = Calendar.getInstance();
//        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
//                calendar = new GregorianCalendar( year, month, dayOfMonth );
//            }
//        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                calendarView.setDate(Calendar.getInstance().getTimeInMillis(),false,true);
                Date date = Calendar.getInstance().getTime();
                caldroidFragment.moveToDate(date);
//                calendar.setTime(date);
            }
        });
//        select_day.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchDate("yyyy-MM-dd");
//            }
//        });
        select_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str_date = "01-"+caldroidFragment.getMonth()+"-"+caldroidFragment.getYear();
                    System.out.println(str_date);
                    searchDate(df.parse(str_date), "yyyy-MM-");
                } catch (ParseException p) {
                    p.printStackTrace();
                }
            }
        });
        select_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String str_date = "01-"+caldroidFragment.getMonth()+"-"+caldroidFragment.getYear();
                    System.out.println(str_date);
                    searchDate(df.parse(str_date), "yyyy-");
                } catch (ParseException p) {
                    p.printStackTrace();
                }
            }
        });
        alertDialog = new AlertDialog.Builder(HistoryActivity.this).create();

        caldroidFragment.setCaldroidListener(new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                searchDate(date, "yyyy-MM-dd");
            }
        });

        SystemUtils.getSQLiteManager().getDataHistory("", new SQLiteManager.ResultDatabase<List<HistoryModel>>() {

            @Override
            public void getData(List<HistoryModel> data) {
                System.out.println(SystemUtils.getGson().toJson(data));
                Map<Date,Drawable> mapDateBG = new HashMap<>();
                Map<Date,Integer> mapDateText = new HashMap<>();
                ColorDrawable blue = new ColorDrawable(SystemUtils.getColor(R.color.caldroid_sky_blue));
                for(HistoryModel map : data) {
                    mapDateText.put(map.getDataTime(), R.color.caldroid_white);
                    mapDateBG.put(map.getDataTime(),blue);
                }
                caldroidFragment.setBackgroundDrawableForDates(mapDateBG);
                caldroidFragment.setTextColorForDates(mapDateText);
                caldroidFragment.refreshView();
            }
        });
    }

    void searchDate(Date date, String format_date) {

        SimpleDateFormat df = new SimpleDateFormat(format_date, new Locale("th", "TH"));
        SystemUtils.getSQLiteManager().getDataHistory(df.format(date), new SQLiteManager.ResultDatabase<List<HistoryModel>>() {
            List<HistoryModel> localData;

            @Override
            public void getData(List<HistoryModel> data) {
                localData = data;
                SystemUtils.showLongToast("ผลการค้นหารายงาน = "+data.size());
                if(data.size()>0) {
                    if(data.size()>1) {
                        try {
                            String[] names = new String[data.size()];

                            for(int i=0;i<data.size();i++) {
                                names[i] = hdf.format(data.get(i).getDataTime());
                            }
                            LayoutInflater inflater = getLayoutInflater();
                            View convertView = inflater.inflate(R.layout.select_list, null);
                            alertDialog.setView(convertView);
                            alertDialog.setTitle("เลือกเวลา");
                            alertDialog.setIcon(android.R.drawable.ic_dialog_info);
                            ListView lv = (ListView) convertView.findViewById(R.id.listView);
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(HistoryActivity.this,android.R.layout.simple_list_item_1,names);
                            lv.setAdapter(adapter);
                            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    openHistory(position);
                                    alertDialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    } else if(data.size()==1) {
                        openHistory(0);
                    }
                }
            }

            void openHistory(int position) {
                if(localData!=null && localData.get(position)!=null) {
                    HistoryModel historyModel = localData.get(position);
                    Intent intent = new Intent(HistoryActivity.this,ReportActivity.class);
                    intent
                            .putExtra(IntentExtraConstant.title, getString(R.string.menu_report)+ " (รายการย้อนหลัง "+hdf.format(historyModel.getDataTime())+")")
                            .putExtra(IntentExtraConstant.bmr_base_calorie, historyModel.getBmr()+"")
                            .putExtra(IntentExtraConstant.bmr_tdee_calorie, historyModel.getTdee()+"")
                            .putExtra(IntentExtraConstant.running_burn_total_energy, historyModel.getTotal_energy()+"")
                            .putExtra(IntentExtraConstant.selected_food_result_calorie, historyModel.getResult_calorie()+"")
                            .putExtra(IntentExtraConstant.report_id, historyModel.getHistoryID())
                            .putExtra(IntentExtraConstant.report_date, historyModel.getDataTime().getTime());
                    startActivityForResult(intent, DELETE_REPORT_RETURN);
                }
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DELETE_REPORT_RETURN && resultCode == RESULT_OK) {
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", new Locale("th", "TH"));
                Date date = new Date(data.getExtras().getLong(IntentExtraConstant.report_date));
                SystemUtils.getSQLiteManager().getDataHistory(df.format(date), new SQLiteManager.ResultDatabase<List<HistoryModel>>() {

                    @Override
                    public void getData(List<HistoryModel> data) {
                        if(data.size()==0) {
                            Date reportDate = data.get(0).getDataTime();
                            caldroidFragment.clearBackgroundDrawableForDate(reportDate);
                            caldroidFragment.clearTextColorForDate(reportDate);
                            caldroidFragment.refreshView();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
