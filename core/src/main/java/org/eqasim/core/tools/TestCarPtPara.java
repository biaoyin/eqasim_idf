package org.eqasim.core.tools;

public class TestCarPtPara {
    static  double car_pt_constant = 0.;

    public TestCarPtPara(){}
    public void setPara(double a) {
        car_pt_constant = a;
    }
    public static double getPara() {
        return  car_pt_constant;
    }
}
