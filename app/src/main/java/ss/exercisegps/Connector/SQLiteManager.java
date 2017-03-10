package ss.exercisegps.Connector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ss.exercisegps.Constants.DBConstants;
import ss.exercisegps.DBModel.HistoryModel;
import ss.exercisegps.Utillities.LoadingDialog;
import ss.exercisegps.Utillities.SystemUtils;


public class SQLiteManager extends SQLiteOpenHelper {
    SimpleDateFormat hdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("th", "TH"));

    public interface ResultDatabase<T> {
        void getData(T data);
    }

    public SQLiteManager(Context context){
        super(context, DBConstants.DB_NAME, null, DBConstants.DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConstants.CREATE_TABLE_HISTORY);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        onCreate(db);

    }

    public void addDataToDB(String table,ContentValues content){
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            db.replace(table,null,content);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
        }
    }

    public void insertDataDB(String table, Map<String, String> data) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "INSERT OR REPLACE INTO "+ table +" ([:KEYS]) VALUES ([:VALUES])";
        String key = "";
        String value = "";
        for(Map.Entry<String,String> entry : data.entrySet()) {
            key += (key.equals("")?"":",")+entry.getKey();
            value += (value.equals("")?"":",")+"'"+entry.getValue()+"'";
        }
        sql = sql.replace("[:KEYS]",key);
        sql = sql.replace("[:VALUES]",value);
        System.out.println("SQL : "+sql);
        try {
            db.execSQL(sql);
            SystemUtils.showLongToast("บันทึกสำเร็จ");
        } catch (Exception e){
            e.printStackTrace();
            SystemUtils.showLongToast("บันทึกล้มเหลว");
        }
        db.close();
    }

    public boolean deleteDataDB(String table,String[] whereClauses, String[] whereArgs) {
        int result = 0;
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM "+ table;
        String whereClause = "";
        for(String str : whereClauses) {
            whereClause += ("".equals(whereClause)?"":" and ")+str+"=?";
        }
        String whereCondition = "[";
        if(whereArgs!=null && whereArgs.length>0) {
            sql += " WHERE "+whereClause;
            for(String str : whereArgs) {
                whereCondition += " "+str;
            }
        }
        sql += " "+whereCondition+"]";
        System.out.println("SQL : "+sql);
        try {
            result = db.delete(table,whereClause, whereArgs);
        } catch (Exception e){
            e.printStackTrace();
        }
        db.close();
        return result>0;
    }

    public List<Map<String,String>> getDataDB(String table,String[] whereKeyValue) {
        SQLiteDatabase db = getReadableDatabase();
        List<Map<String,String>> listMap = new ArrayList<>();
        try {
            String sql = "SELECT * FROM "+table;
            String whereCondition = "";
            if(whereKeyValue!=null && whereKeyValue.length>0) {
                sql += " WHERE";
                for(String str : whereKeyValue) {
                    whereCondition += " "+str;
                }
            }
            sql += whereCondition;
            System.out.println("SQL : "+sql);
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                do {
                    Map<String,String> map = new HashMap<>();
                    for(int i=0;i<cursor.getColumnCount();i++) {
                        map.put(cursor.getColumnName(i),cursor.getString(i));
                    }
                    listMap.add(map);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return listMap;
    }

    public void addDataHistory(HistoryModel historyModel) {
        Map<String,String> mapInsert = new Hashtable<>();
//        ContentValues contentValues = new ContentValues();
        if(historyModel.getHistoryID()!=null) {
            mapInsert.put(DBConstants.Key_History_ID, ""+historyModel.getHistoryID());
        }
        if(historyModel.getDataTime()!=null) {
            mapInsert.put(DBConstants.History_DateTime, hdf.format(historyModel.getDataTime()));
        } else {
            mapInsert.put(DBConstants.History_DateTime, hdf.format(new Date()));
        }
        if(historyModel.getBmr()!=null) {
            mapInsert.put(DBConstants.History_BMR, ""+historyModel.getBmr());
        }
        if(historyModel.getTdee()!=null) {
            mapInsert.put(DBConstants.History_TDEE, ""+historyModel.getTdee());
        }
        if(historyModel.getTotal_energy()!=null) {
            mapInsert.put(DBConstants.History_Total_Energy, ""+historyModel.getTotal_energy());
        }
        if(historyModel.getResult_calorie()!=null) {
            mapInsert.put(DBConstants.History_Result_Calorie, ""+historyModel.getResult_calorie());
        }
        insertDataDB(DBConstants.Table_History, mapInsert);
//        addDataToDB(DBConstants.Table_History, contentValues);
    }

    public boolean deleteDataHistory(int history_id) {
        return deleteDataDB(
                DBConstants.Table_History
                , new String[]{DBConstants.Key_History_ID}
                , new String[]{history_id+""});
    }

    public void getDataHistory(String str_date, final ResultDatabase<List<HistoryModel>> resultDatabase) {
        LoadingDialog.show();
        new AsyncTask<String,Void,List<HistoryModel>>() {

            @Override
            protected List<HistoryModel> doInBackground(String... params) {
                String[] arr_str = null;
                if(!params[0].equals("")) {
                    arr_str = new String[]{DBConstants.History_DateTime+" LIKE '%"+params[0]+"%'"};
                }
                List<Map<String,String>> mapList = getDataDB(DBConstants.Table_History, arr_str);
                List<HistoryModel> historyModelList = new ArrayList<>();
                for(Map<String,String> map : mapList) {
                    HistoryModel historyModel = new HistoryModel();
                    historyModel.setHistoryID(Integer.parseInt(map.get(DBConstants.Key_History_ID)));
                    try {
                        historyModel.setDataTime(hdf.parse(map.get(DBConstants.History_DateTime)));
                    } catch (ParseException p) {
                        historyModel.setDataTime(new Date());
                    }
                    historyModel.setBmr(Float.parseFloat(map.get(DBConstants.History_BMR)));
                    historyModel.setTdee(Float.parseFloat(map.get(DBConstants.History_TDEE)));
                    historyModel.setTotal_energy(Float.parseFloat(map.get(DBConstants.History_Total_Energy)));
                    historyModel.setResult_calorie(Float.parseFloat(map.get(DBConstants.History_Result_Calorie)));
                    historyModelList.add(historyModel);
                }
                return historyModelList;
            }

            @Override
            protected void onPostExecute(List<HistoryModel> result) {
                resultDatabase.getData(result);
                LoadingDialog.close();
            }
        }.execute(str_date);
    }
}

