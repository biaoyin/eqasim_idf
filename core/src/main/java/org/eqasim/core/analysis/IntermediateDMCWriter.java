package org.eqasim.core.analysis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//BYIN: 09/04/2024: when trying to save intermediate modal travel time (car, pt, and bike set in *UtilityEstimator), we need to modify the tour length value being 1000 (big enough) in TourLengthFilter.

public class IntermediateDMCWriter {
    final private List<String> modeTravelTimes;
    final private String delimiter = ";";

    public IntermediateDMCWriter (List<String> modeTravelTimes) {
        this.modeTravelTimes = modeTravelTimes;
    }

    public void write(String outputPath) throws IOException {

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        writer.write(formatHeader() + "\n");
        writer.flush();
        for (int i = 0; i < modeTravelTimes.size(); i++) {
            writer.write(modeTravelTimes.get(i) + "\n");
            writer.flush();
        }

    }

    private String formatHeader() {
        return String.join(delimiter, new String[] { //
                "person_id", //
                "tripDepTime", //
                "travelTime_min", //
//                "origin_x", //
//                "origin_y", //
//                "destination_x", //
//                "destination_y", "departure_time", //
//                "travel_time", //
//                "vehicle_distance", //
//                 "routed_distance" //
//                "mode", //
//                "preceding_purpose", //
//                "following_purpose", //
//                "returning", //
                "euclidean_distance" //
        });
    }

}
