package ss.exercisegps.Utillities;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    private double min, max;

    public InputFilterMinMax(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String text = dest.toString() + source.toString();
        try {
            int count = text.length() - text.replace(".", "").length();
            if ((count == 1 && text.substring(text.length() - 1).equals(".")) || isInRange(min, max, Double.parseDouble(text))) {
                return null;
            }
        } catch (NumberFormatException nfe) {

        }
        return "";
    }

    private boolean isInRange(double a, double b, double c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
