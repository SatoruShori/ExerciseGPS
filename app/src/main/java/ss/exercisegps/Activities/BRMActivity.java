package ss.exercisegps.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import ss.exercisegps.Constants.ShareConstants;
import ss.exercisegps.R;
import ss.exercisegps.Utillities.InputFilterMinMax;
import ss.exercisegps.Utillities.SystemUtils;

public class BRMActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    EditText et_age
            , et_weight
            , et_height;

    RadioGroup rg_gender;

    Spinner spin_variant;

    Button bt_calculator;

    TextView tw_base_calorie
            ,tw_total_calorie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brm);
        SystemUtils.setActivity(this);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        et_age = (EditText)findViewById(R.id.bmr_age);
        et_weight = (EditText)findViewById(R.id.bmr_weight);
        et_height = (EditText)findViewById(R.id.bmr_height);
        rg_gender = (RadioGroup)findViewById(R.id.bmr_gender);
        spin_variant = (Spinner)findViewById(R.id.bmr_variant);
        bt_calculator = (Button)findViewById(R.id.bmr_calculator);
        tw_base_calorie = (TextView)findViewById(R.id.bmr_base_calorie);
        tw_total_calorie = (TextView)findViewById(R.id.bmr_tdee_calorie);

        et_age.setFilters(new InputFilter[]{new InputFilterMinMax(0,99)});
        et_weight.setFilters(new InputFilter[]{new InputFilterMinMax(0,999)});
        et_height.setFilters(new InputFilter[]{new InputFilterMinMax(0,999)});

        ArrayAdapter<CharSequence> varAdapter = ArrayAdapter.createFromResource(
                this, R.array.bmr_variant, R.layout.spinner_layout);
        varAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spin_variant.setAdapter(varAdapter);

        bt_calculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    Double age = 0D
                            , weight = 0D
                            , height = 0D;
                    String str_value;
                    str_value = et_age.getText().toString();
                    if(!str_value.equals("")) {
                        age = Double.parseDouble(str_value);
                    }
                    str_value = et_weight.getText().toString();
                    if(!str_value.equals("")) {
                        weight = Double.parseDouble(str_value);
                    }
                    str_value = et_height.getText().toString();
                    if(!str_value.equals("")) {
                        height = Double.parseDouble(str_value);
                    }

                    int variant_position = spin_variant.getSelectedItemPosition();
                    Double variant = variant_position==1?1.2
                            :variant_position==2?1.357
                            :variant_position==3?1.55
                            :variant_position==4?1.725
                            :variant_position==5?1.9
                            :1;

                    Double bmr = null;
                    int gender_id = rg_gender.getCheckedRadioButtonId();

                    if(gender_id==R.id.bmr_male) {
                        bmr = 66+(13.7*weight)+(5*height)-(6.8*age);
                    } else if(gender_id==R.id.bmr_female) {
                        bmr = 665 + (9.6*weight)+(1.8*height)-(4.7*age);
                    }

                    try {
                        tw_base_calorie.setText(String.format(Locale.ROOT,"%.2f",Float.parseFloat(bmr+"")));
                        tw_total_calorie.setText(String.format(Locale.ROOT,"%.2f",Float.parseFloat((bmr*variant)+"")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        et_age.setText(sharedPreferences.getString(ShareConstants.BMR_AGE,""));
        et_weight.setText(sharedPreferences.getString(ShareConstants.BMR_WEIGHT,""));
        et_height.setText(sharedPreferences.getString(ShareConstants.BMR_HEIGHT,""));
        rg_gender.check(sharedPreferences.getInt(ShareConstants.BMR_GENDER_ID,-1));
        spin_variant.setSelection(sharedPreferences.getInt(ShareConstants.BMR_VARIANT,0));
        tw_base_calorie.setText(sharedPreferences.getString(ShareConstants.BMR_BASE_CALORIE,""));
        tw_total_calorie.setText(sharedPreferences.getString(ShareConstants.BMR_TOTAL_CALORIE,""));
    }

    boolean validate(){
        EditText[] allET = {et_age, et_weight, et_height};

        int result = 0;
        String toast = "";
        for(EditText editText : allET) {
            try {
                String str_value = editText.getText().toString();
                if(!str_value.equals("")) {
                    Double value = Double.parseDouble(str_value);
                    if(value<=0) {
                        result++;
                        TextView header = (TextView)((ViewGroup)editText.getParent()).getChildAt(0);
                        toast += ("".equals(toast)?"":"\n")+"กรุณากรอก"+header.getText().toString();
                    }
                } else {
                    result++;
                    TextView header = (TextView)((ViewGroup)editText.getParent()).getChildAt(0);
                    toast += ("".equals(toast)?"":"\n")+"กรุณากรอก"+header.getText().toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (rg_gender.getCheckedRadioButtonId() == -1) {
            result++;
            toast += ("".equals(toast)?"":"\n")+"กรุณาเลือกเพศ";
        }
        if(spin_variant.getSelectedItemPosition()==0) {
            result++;
            toast += ("".equals(toast)?"":"\n")+"กรุณาเลือกตัวแปร";
        }
        if(toast.length()>0) {
            SystemUtils.showLongToast(toast);
        }
        return result==0;
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
        String gender_str = ((RadioButton)rg_gender.findViewById(rg_gender.getCheckedRadioButtonId())).getText().toString();
        sharedPreferences.edit()
                .putString(ShareConstants.BMR_AGE, et_age.getText().toString())
                .putString(ShareConstants.BMR_GENDER, gender_str)
                .putString(ShareConstants.BMR_WEIGHT, et_weight.getText().toString())
                .putString(ShareConstants.BMR_HEIGHT, et_height.getText().toString())
                .putInt(ShareConstants.BMR_GENDER_ID, rg_gender.getCheckedRadioButtonId())
                .putInt(ShareConstants.BMR_VARIANT, spin_variant.getSelectedItemPosition())
                .putString(ShareConstants.BMR_BASE_CALORIE, tw_base_calorie.getText().toString())
                .putString(ShareConstants.BMR_TOTAL_CALORIE, tw_total_calorie.getText().toString())
                .apply();
        super.onStop();
    }
}
