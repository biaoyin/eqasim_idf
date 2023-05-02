package org.eqasim.ile_de_france.mode_choice.parameters;

public class TestTollFee {
    static  double fee_toll = 0.;
    static  String toll_area_shapefile;

    public TestTollFee(){}

    public void setTollFee(double var1) {
        fee_toll = var1;
    }
    public static double getTollFee() {
        return  fee_toll;
    }

    public void setTollAreaFilePath(String var2) {
        toll_area_shapefile = var2;
    }
    public static String getTollAreaFilePath() {
        return  toll_area_shapefile;
    }
}
