package ss.exercisegps.Utillities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ss.exercisegps.Connector.SQLiteManager;
import ss.exercisegps.R;

public class SystemUtils {
    private static Activity activity;
    private static NetworkInfo networkInfo;
//    private static TelephonyManager telephonyManager;
    private static Gson gson = new Gson();
    private static SQLiteManager sqLiteManager;
    private static Point size = new Point();
//    private static SimpleDateFormat utcDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private static final String valid_until = "25/7/2016";

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    public static void setActivity(Activity activity) {
//        try {
//            Date strDate = sdf.parse(valid_until);
//            if (new Date().after(strDate)) {
//                System.out.println("Expire App");
//            } else {
//                SystemUtils.activity = activity;
////                telephonyManager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        SystemUtils.activity = activity;
    }

    public static Activity getActivity() {
//        try {
//            Date strDate = sdf.parse(valid_until);
//            if (new Date().after(strDate)) {
//                System.out.println("Expire App");
//                return null;
//            } else {
//                return activity;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//            return null;
//        }
        return activity;
    }

    public static SharedPreferences getSharedPreferences(String key) {
        return activity.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public static void setNetworkInfo(NetworkInfo networkInfo) {
        SystemUtils.networkInfo = networkInfo;
    }

    public static NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public static boolean isConnected() {
        if(networkInfo!=null) {
            return networkInfo.isConnected();
        } else {
            ConnectivityManager cm =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
    }

    public static boolean isNowNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni==null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasCamera() {
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }

    public static int dp2px(int dp) {
        int px = Math.round(dp * activity.getResources().getDisplayMetrics().density);
        return px;
    }

    public static int px2dp(int px) {
        int dp = Math.round(px / activity.getResources().getDisplayMetrics().density);
        return dp;
    }

    public static String setDigit(int input, int size) {
        String output = "00000000000" + input;
        output = output.substring(output.length() - size);
        return output;
    }

    public static void showLongToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
    }

    public static void showShortToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

    public static Gson getGson() {
        return gson;
    }

    public static SQLiteManager getSQLiteManager() {
        if(sqLiteManager==null) {
            sqLiteManager = new SQLiteManager(activity);
        }
        return sqLiteManager;
    }

    public static void setBgView(View view,Drawable sb){
        if(Build.VERSION.SDK_INT < 16){
            view.setBackgroundDrawable(sb);
        }else{
            view.setBackground(sb);
        }
    }

    public static Drawable getDrawable(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getResources().getDrawable(id, activity.getTheme());
        } else {
            return activity.getDrawable(id);
        }
    }

    public static boolean isInternetAvailable() {
        boolean server = false;
        String netAddress = "";
        try {

            boolean net = isConnected();
            if (net) {
                netAddress = new AsyncTask<String, Integer, String>(){
                    @Override
                    protected String doInBackground(String... params) {
                        InetAddress addr = null;
                        try {
                            addr = InetAddress.getByName(params[0]);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        if (addr == null) {
                            return "";
                        }
                        return addr.getHostAddress();
                    }
                }.execute("app.gpsshadow.com").get();
                System.out.println(netAddress);
            } else {
                return server;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if ("".equals(netAddress) || netAddress == null) {
            server = false;
        } else {
            server = true;
        }
        return server;
    }

    public static String getAppVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte bytes) {
        byte[] newByte = {bytes};
        return bytesToHex(newByte);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static float hexToFloat(String hex) {
        int i = Integer.parseInt(hex, 16);
        return Float.intBitsToFloat(i);
    }

    public static float hexToFloat(byte[] bytes) {
        return hexToFloat(bytesToHex(bytes));
    }

    public static Point getRotationSize() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getSize(size);
        return size;
    }

    public static void setChangeHeightSizeView(ViewGroup viewGroup, int reduceSize) { //reduceSize = DP1
        ViewGroup.LayoutParams newLP = viewGroup.getLayoutParams();
        viewGroup.measure(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        Point point = getRotationSize();
        int minSize = point.y-reduceSize;
        if(viewGroup.getMeasuredHeight()>minSize) {
            newLP.height = minSize;
        } else {
            newLP.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        viewGroup.setLayoutParams(newLP);
    }

    public static boolean isJSONValid(String jsonStr) {
        try {
            new JSONObject(jsonStr);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(jsonStr);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static void disableEnableViewGroup(ViewGroup viewGroup, boolean enable){
        if(viewGroup.isEnabled()!=enable) {
            for (int i = 0; i < viewGroup.getChildCount(); i++){
                View child = viewGroup.getChildAt(i);
                child.setEnabled(enable);
                if (child instanceof ViewGroup){
                    disableEnableViewGroup((ViewGroup) child, enable);
                }
            }
            viewGroup.setEnabled(enable);
        }
    }

    public static Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Uri uri) {
        if("content".equalsIgnoreCase(uri.getScheme())) {
            try {
                Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(idx);
                }
            } catch (Exception e) {

            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static String convertToStringRepresentation(final long value){
        final long[] dividers = new long[] { T, G, M, K, 1 };
        final String[] units = new String[] { "TB", "GB", "MB", "KB", "B" };
        if(value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for(int i = 0; i < dividers.length; i++){
            final long divider = dividers[i];
            if(value >= divider){
                double valueSize = divider > 1 ? (double) value / (double) divider : (double) value;
                result = new DecimalFormat("#,##0.#").format(valueSize) + " " + units[i];
                break;
            }
        }
        return result;
    }

    public static List<File> getAllImageInsideFolder(String path) {
        final String header = "ScoreBarcode"
                , separate = "/";
        List<File> bitmaps = new ArrayList<>();
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        path = (path.charAt(0)=='/'?"":"/")+path;
        File folder = new File(root,header+path);
        if(folder.exists()) {
            for(File file : folder.listFiles()) {
                if(file.isDirectory()) {
                    String folderPath = file.getAbsolutePath().replace(root.getAbsolutePath()+separate+header,"");
                    bitmaps.addAll(getAllImageInsideFolder(folderPath));
                } else {
                    if(file.getName().toLowerCase().endsWith(".jpg")) {
                        bitmaps.add(file);
                    }
                }

            }
        }
        return bitmaps;
    }

    public static String stringAppend(String base, String append) {
        return base + (base.length()>0?"\n":"")+append;
    }

    public static void showHideMenu(final View content, final int visibility) {
        if(visibility==View.VISIBLE) {
            content.setVisibility(visibility);
            content.setAlpha(0.0f);
            content.animate().alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
        } else {
            content.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    content.setVisibility(visibility);
                }
            });
        }
    }

    public static <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static void addView(ViewGroup parent, View view) {
        ViewGroup viewParent = (ViewGroup)view.getParent();
        if(viewParent!=null) {
            viewParent.removeView(view);
        }
        parent.addView(view);
    }

    public static int getColor(int id) {
        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = activity.getColor(id);
        } else {
            color = activity.getResources().getColor(id);
        }
        return color;
    }

    public static int getColor(int id, float ratio) {
        return getColorWithAlpha(getColor(id), ratio);
    }

    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    public static Bitmap getBitmapFromPath(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither=false;                     //Disable Dithering mode
            try {
                options.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
                options.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
            } catch (Exception e) {

            }
            options.inTempStorage=new byte[32 * 1024];
            options.inSampleSize = 2;
            return BitmapFactory.decodeStream(fis, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromName(String name) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither=false;                     //Disable Dithering mode
            try {
                options.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
                options.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
            } catch (Exception e) {

            }
            options.inTempStorage=new byte[32 * 1024];
            options.inSampleSize = 2;
            int drawableResourceId = activity.getResources().getIdentifier(name, "drawable", activity.getPackageName());
            return BitmapFactory.decodeResource(activity.getResources(), drawableResourceId, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setFullSizeImageFromPath(final ImageView imageView, String path) {
        new AsyncTask<String,Void,Bitmap>() {

            @Override
            protected void onPreExecute() {
                Animation animRotate = AnimationUtils.loadAnimation(SystemUtils.getActivity(), R.anim.rotate);
                imageView.setImageResource(R.drawable.ic_loading_sun);
                imageView.startAnimation(animRotate);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                return getBitmapFromPath(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.clearAnimation();
                imageView.setImageBitmap(bitmap);
            }
        }.execute(path);

    }

    public static void setFullSizeImageFromName(final ImageView imageView, String name) {
        new AsyncTask<String,Void,Bitmap>() {

            @Override
            protected void onPreExecute() {
                Animation animRotate = AnimationUtils.loadAnimation(SystemUtils.getActivity(), R.anim.rotate);
                imageView.setImageResource(R.drawable.ic_loading_sun);
                imageView.startAnimation(animRotate);
            }

            @Override
            protected Bitmap doInBackground(String... params) {
                return getBitmapFromName(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                imageView.clearAnimation();
                imageView.setImageBitmap(bitmap);
            }
        }.execute(name);

    }

    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(int resourceId, int reqWidth, int reqHeight) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(activity.getResources(), resourceId, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(activity.getResources(), resourceId, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void setSimpleImage(final ImageView imageView, String path, int reqWidth, int reqHeight) {

        new AsyncTask<Object,Void,Bitmap>() {

            @Override
            protected void onPreExecute() {
                Animation animRotate = AnimationUtils.loadAnimation(SystemUtils.getActivity(), R.anim.rotate);
                imageView.setImageResource(R.drawable.ic_loading_sun);
                imageView.startAnimation(animRotate);
            }

            @Override
            protected Bitmap doInBackground(Object... params) {
                return decodeSampledBitmapFromPath((String)params[0],(int)params[1],(int)params[2]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if(bitmap!=null) {
                    imageView.clearAnimation();
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute(path,reqWidth,reqHeight);

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static void moveView(View view, ViewGroup newParent) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        newParent.addView(view);
    }

    public static boolean checkLanguage(String s,Character.UnicodeBlock unicodeBlock) {
        if(s.length()>0) {
            for ( char c : s.toCharArray() ) {
                if (Character.UnicodeBlock.of(c) != unicodeBlock) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static String checkNetworkType() {
        NetworkInfo networkInfo = getNetworkInfo();
        String network_type = "";
        if(networkInfo.isConnected()) {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                network_type =  "WIFI";
            }
            if(networkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: { //api<8 : replace by 11
                        network_type =  "2G";
                        break;
                    }
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B://api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD://api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP: { //api<13 : replace by 15
                        network_type = "3G";
                        break;
                    }
                    case TelephonyManager.NETWORK_TYPE_LTE: { //api<11 : replace by 13
                        network_type = "4G";
                        break;
                    }
                    default: {
                        network_type = "?";
                    }
                }
            }
        }
        return network_type;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
