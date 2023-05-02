package org.eqasim.core.tools;

public class TestCarPtPara {
    static  double car_pt_constant = 0.;
    static  String car_pt_savepath;

    public TestCarPtPara(){}

    public void setPara(double var1) {
        car_pt_constant = var1;
    }
    public static double getPara() {
        return  car_pt_constant;
    }
    public void setCarPtSavePath(String var2) {
        car_pt_savepath = var2;
    }
    public static String getCarPtSavePath() {
        return  car_pt_savepath;
    }

}
