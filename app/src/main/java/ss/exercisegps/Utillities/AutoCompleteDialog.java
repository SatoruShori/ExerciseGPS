package ss.exercisegps.Utillities;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ss.exercisegps.R;


public class AutoCompleteDialog extends DialogFragment {
    private static AutoCompleteDialog autoCompleteDialog;
    private static boolean isShow = false;
    private static CenterTextArrayAdapter textArrayAdapter = new CenterTextArrayAdapter();
    private static List<String> indexString = new ArrayList<>();
    private static AutoCompleteTextView autoCompleteTextView;

    public interface CallBack {
        void onClick(int position, String value);
    }

    private static CallBack callBack;

    private static AutoCompleteDialog getInstance() {
        if(autoCompleteDialog ==null) {
            autoCompleteDialog =  new AutoCompleteDialog();
        }
        if(autoCompleteTextView!=null) {
            autoCompleteTextView.setText("");
        }
        return autoCompleteDialog;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        GradientDrawable sp = new GradientDrawable();
        sp.setColor(Color.TRANSPARENT);
        dialog.getWindow().setBackgroundDrawable(sp);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_autocomplete, container);
        autoCompleteTextView = (InstantAutoComplete)view.findViewById(R.id.autocomplete);
        autoCompleteTextView.setAdapter(textArrayAdapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                close();
                String value = textArrayAdapter.getItem(position);
                AutoCompleteDialog.callBack.onClick(indexString.indexOf(value), value);
            }
        });
        return view;
    }

    public static void show(List<String> stringList, CallBack callBack) {
        try {
            if(!isShow || !getInstance().isVisible()) {
                isShow = true;
                AutoCompleteDialog.callBack = callBack;
                textArrayAdapter.clear();
                textArrayAdapter.addAll(stringList);
                getInstance().show(SystemUtils.getActivity().getFragmentManager(), "autoCompleteDialog");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show(List<Map<String,String>> mapList,String key, CallBack callBack) {
        try {
            if(!isShow || !getInstance().isVisible()) {
                isShow = true;
                AutoCompleteDialog.callBack = callBack;
                textArrayAdapter.clear();
                indexString.clear();
                for(Map<String,String> map : mapList) {
                    indexString.add(map.get(key));
                }
                textArrayAdapter.addAll(indexString);
                textArrayAdapter.notifyDataSetChanged();
                getInstance().show(SystemUtils.getActivity().getFragmentManager(), "autoCompleteDialog");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            if(isShow || getInstance().isVisible()) {
                isShow = false;
                getInstance().dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
