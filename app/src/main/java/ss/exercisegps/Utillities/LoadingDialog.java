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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.wang.avi.AVLoadingIndicatorView;

import ss.exercisegps.R;


public class LoadingDialog extends DialogFragment {
    private static LoadingDialog loadingDialog;
    private static boolean isShow = false;

    private static LoadingDialog getInstance() {
        if(loadingDialog==null) {
            loadingDialog =  new LoadingDialog();
            loadingDialog.setCancelable(false);
        }
        return loadingDialog;
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
        View view = inflater.inflate(R.layout.dialog_loading, container);
        AVLoadingIndicatorView avi = (AVLoadingIndicatorView)view.findViewById(R.id.avi);
//        avi.smoothToShow();
        return view;
    }

    public static void show() {
        try {
            if(!isShow || !getInstance().isVisible()) {
                isShow = true;
                getInstance().show(SystemUtils.getActivity().getFragmentManager(), "loadingDialog");
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
