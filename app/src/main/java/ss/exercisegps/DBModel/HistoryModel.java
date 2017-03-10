package ss.exercisegps.DBModel;

import java.util.Date;

/**
 * Created by SatoruShori on 11/12/2559.
 */

public class HistoryModel {
    Integer historyID;
    Date dataTime;
    Float bmr, tdee, total_energy, result_calorie;

    public Integer getHistoryID() {
        return historyID;
    }

    public Date getDataTime() {
        return dataTime;
    }

    public Float getBmr() {
        return bmr;
    }

    public Float getTdee() {
        return tdee;
    }

    public Float getTotal_energy() {
        return total_energy;
    }

    public Float getResult_calorie() {
        return result_calorie;
    }

    public void setHistoryID(Integer historyID) {
        this.historyID = historyID;
    }

    public void setDataTime(Date dataTime) {
        this.dataTime = dataTime;
    }

    public void setBmr(Float bmr) {
        this.bmr = bmr;
    }

    public void setTdee(Float tdee) {
        this.tdee = tdee;
    }

    public void setTotal_energy(Float total_energy) {
        this.total_energy = total_energy;
    }

    public void setResult_calorie(Float result_calorie) {
        this.result_calorie = result_calorie;
    }
}
