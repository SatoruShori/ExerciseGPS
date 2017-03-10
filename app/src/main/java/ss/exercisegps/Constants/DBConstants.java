package ss.exercisegps.Constants;

public class DBConstants {
    public static final String DB_NAME = "EXERCISE_GPS";
    public static final int DB_VERSION = 1;

    //Table Name
    public static final String Table_History = "HISTORY";

    //Table History Column
    public static final String Key_History_ID = "History_ID"
            , History_DateTime = "History_DateTime"
            , History_BMR = "History_BMR"
            , History_TDEE = "History_TDEE"
            , History_Total_Energy = "History_Total_Energy"
            , History_Result_Calorie = "History_Result_Calorie";


    //Create Table
    public static final String CREATE_TABLE_HISTORY = "CREATE TABLE IF NOT EXISTS "
            + Table_History + "(" +
            Key_History_ID + " INTEGER PRIMARY KEY" +
            "," +
            History_DateTime + " DATETIME" +
            "," +
            History_BMR + " REAL" +
            "," +
            History_TDEE + " REAL" +
            "," +
            History_Total_Energy + " REAL" +
            "," +
            History_Result_Calorie + " REAL" +
            ")";
}
