package ss.exercisegps.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ss.exercisegps.Constants.ShareConstants;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.AutoCompleteDialog;
import ss.exercisegps.Utillities.LoadingDialog;
import ss.exercisegps.Utillities.SystemUtils;

public class SelectedFoodActivity extends AppCompatActivity {

    LayoutInflater inflater;
    List<Map<String,String>> foodList = new ArrayList<>();
    List<Map<String,String>> selectedFoodList = new ArrayList<>();

    SharedPreferences sharedPreferences;

    LinearLayout selected_food_content;
    TextView selected_food_result;

    Button addFood;

    Integer calorie_result = 0;

    String keyName = "name"
            , keyUnit = "unit"
            , keyCalorie = "calorie";

    public class FoodItem {
        View view;
        TextView tw_name, tw_unit, tw_calorie;
        Button delete;

        FoodItem(Map<String,String> data) {
            try {
                int numberIndex = selected_food_content.getChildCount();
                if(numberIndex>0) {
                    View v = new View(SelectedFoodActivity.this);
                    v.setBackgroundResource(android.R.color.darker_gray);
                    v.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,  SystemUtils.dp2px(2)));
                    selected_food_content.addView(v);
                }
//                Map<String,String> data = foodList.get(position);

                view = inflater.inflate(R.layout.food_text_item, null, false);
                view.setTag(data);
                tw_name = (TextView) view.findViewById(R.id.food_name);
                tw_unit = (TextView) view.findViewById(R.id.food_unit);
                tw_calorie = (TextView) view.findViewById(R.id.food_calorie);
                delete = (Button) view.findViewById(R.id.food_delete);

                String cal = data.get(keyCalorie);
                tw_name.setText(data.get(keyName));
                tw_unit.setText(data.get(keyUnit));
                tw_calorie.setText(cal);

                String cal2 = cal.split(" ")[0];
                if(SystemUtils.isNumeric(cal2)) {
                    try {
                        Integer integer = Integer.parseInt(cal2);
                        calorie_result += integer;
                        selected_food_result.setText(""+calorie_result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String,String> map = (Map<String,String>)view.getTag();
                        selectedFoodList.remove(map);
                        int dividerIndex = selected_food_content.indexOfChild(view);

                        if(dividerIndex>0) {
                            selected_food_content.removeViewAt(dividerIndex-1);
                        } else if(selected_food_content.getChildCount()>1) {
                            selected_food_content.removeViewAt(dividerIndex+1);
                        }

                        selected_food_content.removeView(view);
                        String cal = tw_calorie.getText().toString();
                        String cal2 = cal.split(" ")[0];
                        if(SystemUtils.isNumeric(cal2)) {
                            try {
                                Integer integer = Integer.parseInt(cal2);
                                calorie_result -= integer;
                                selected_food_result.setText(""+calorie_result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                view.setOnClickListener(new View.OnClickListener() {
                    EditText input;
                    TextView unit;
                    @Override
                    public void onClick(View v) {
                        Map<String,String> map = (Map<String,String>)view.getTag();
                        LayoutInflater inflater = getLayoutInflater();
                        View convertView = inflater.inflate(R.layout.food_quantity_input, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(SelectedFoodActivity.this);
                        builder.setView(convertView);
                        builder.setTitle(map.get(keyName));
                        builder.setIcon(android.R.drawable.ic_dialog_dialer);
                        input = (EditText) convertView.findViewById(R.id.food_input_int);
                        unit = (TextView)convertView.findViewById(R.id.food_input_unit);
                        try {
                            String[] unitText = map.get(keyUnit).split(" ");
                            input.setText(unitText[0]);
                            unit.setText(unitText[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    Map<String,String> map = (Map<String,String>)view.getTag();
                                    selectedFoodList.remove(map);
                                    Integer quantity = Integer.parseInt(input.getText().toString());
                                    String[] oldCal = map.get(keyCalorie).split(" ");
                                    int oldQuantity = Integer.parseInt(map.get(keyUnit).split(" ")[0]);
                                    int originalCal = Integer.parseInt(oldCal[0])/oldQuantity;
                                    String newUnit = quantity + " " + unit.getText().toString()
                                            , newCal = (quantity*originalCal)+ " "+ oldCal[1];
                                    map.put(keyUnit, newUnit);
                                    map.put(keyCalorie, newCal);

                                    String txtCal = tw_calorie.getText().toString().split(" ")[0];

                                    tw_unit.setText(newUnit);
                                    tw_calorie.setText(newCal);

                                    if(SystemUtils.isNumeric(txtCal)) {
                                        try {
                                            Integer integer = Integer.parseInt(txtCal);
                                            calorie_result -= integer;
                                            calorie_result += (quantity*originalCal);
                                            selected_food_result.setText(""+calorie_result);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    String toast =
                                            map.get(keyName)+
                                                    "\n"+
                                                    map.get(keyUnit)+
                                                    "\n"+
                                                    map.get(keyCalorie);
                                    SystemUtils.showLongToast(toast);
                                    selectedFoodList.add(map);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.show();
                    }
                });

                selected_food_content.addView(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_food);
        SystemUtils.setActivity(this);
        inflater = (LayoutInflater) SystemUtils.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sharedPreferences = SystemUtils.getSharedPreferences(getString(R.string.preference_file_key));
        try {        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        addFood = (Button)findViewById(R.id.selected_food_add);
        addFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteDialog.show(foodList, keyName, new AutoCompleteDialog.CallBack() {
                    EditText input;
                    TextView unit;
                    @Override
                    public void onClick(final int position, String value) {
                        try {
                            Map<String,String> mapData = foodList.get(position);
                            LayoutInflater inflater = getLayoutInflater();
                            View convertView = inflater.inflate(R.layout.food_quantity_input, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(SelectedFoodActivity.this);
                            builder.setView(convertView);
                            builder.setTitle(mapData.get(keyName));
                            builder.setIcon(android.R.drawable.ic_dialog_dialer);
                            input = (EditText) convertView.findViewById(R.id.food_input_int);
                            unit = (TextView)convertView.findViewById(R.id.food_input_unit);
                            String[] unitText = mapData.get(keyUnit).split(" ");
                            input.setText(unitText[0]);
                            unit.setText(unitText[1]);
                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Integer quantity = Integer.parseInt(input.getText().toString());
                                    Map<String,String> mapData = foodList.get(position), newMap = new HashMap<>();
                                    String[] cal = mapData.get(keyCalorie).split(" ");
                                    String newUnit = quantity + " " + unit.getText().toString()
                                            , newCal = (quantity*Integer.parseInt(cal[0]))+ " "+ cal[1];
                                    newMap.put(keyName, mapData.get(keyName));
                                    newMap.put(keyUnit, newUnit);
                                    newMap.put(keyCalorie, newCal);

                                    selectedFoodList.add(newMap);

                                    String toast =
                                            newMap.get(keyName)+
                                                    "\n"+
                                                    newMap.get(keyUnit)+
                                                    "\n"+
                                                    newMap.get(keyCalorie);
                                    SystemUtils.showLongToast(toast);
                                    new FoodItem(newMap);
                                }
                            });
                            builder.show();

//                        String toast = "position : " +
//                                position +
//                                "\n" +
//                                "\n" +
//                                mapData.get(keyName)+
//                                "\n"+
//                                mapData.get(keyUnit)+
//                                "\n"+
//                                mapData.get(keyCalorie);
//                        SystemUtils.showLongToast(toast);
//                        new FoodItem(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        selected_food_content = (LinearLayout) findViewById(R.id.selected_food_content);
        selected_food_result = (TextView)  findViewById(R.id.selected_food_result_calorie);

        LoadingDialog.show();
        new AsyncTask<Void,Void,String>() {

            @Override
            protected String doInBackground(Void... params) {
                return readFoodData();
            }

            @Override
            protected void onPostExecute(String result) {

                foodList = SystemUtils.getGson().fromJson(result, new TypeToken<List<Map<String, String>>>(){}.getType());
                System.out.println(SystemUtils.getGson().toJson(foodList));
                try {
                    String oldSelected = sharedPreferences.getString(ShareConstants.SELECTED_FOOD_LIST, "[]");
                    selectedFoodList = SystemUtils.getGson().fromJson(oldSelected, new TypeToken<List<Map<String, String>>>(){}.getType());
                    for(Map<String,String> map : selectedFoodList) {
                        new FoodItem(map);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                LoadingDialog.close();
            }
        }.execute();

    }

    private String readFoodData() {
        InputStream is = getResources().openRawResource(R.raw.food_list);
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
    public void onStop () {
        sharedPreferences.edit()
                .putString(ShareConstants.SELECTED_FOOD_LIST, SystemUtils.getGson().toJson(selectedFoodList))
                .putInt(ShareConstants.SELECTED_FOOD_CALORIE, calorie_result)
                .apply();
        super.onStop();
    }
}
