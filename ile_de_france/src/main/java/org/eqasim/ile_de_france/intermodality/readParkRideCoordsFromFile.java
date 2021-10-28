package org.eqasim.ile_de_france.intermodality;

import org.matsim.api.core.v01.Coord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class readParkRideCoordsFromFile {
    List<Coord> readCoords;
    String file;

    readParkRideCoordsFromFile(String file) throws IOException {
        this.file = file;
        this.readCoords = readCoords(file);
    }

    private List<Coord> readCoords (String file) throws IOException {
        List<Coord> XYCoords = new ArrayList<Coord>();
        List<Double> xCoordList = new ArrayList<Double>();
        List<Double> yCoordList = new ArrayList<Double>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // this will read the first line
            String line = null;
            while ((line = br.readLine()) != null) {
//                xCoordList.add(Double.parseDouble(line.split(";")[0]));
//                yCoordList.add(Double.parseDouble(line.split(";")[1]));
                xCoordList.add(Double.parseDouble(line.split(";")[1]));// add id of the park&ride
                yCoordList.add(Double.parseDouble(line.split(";")[2]));
            }
        } catch (FileNotFoundException e) {
            System.out.println("check out the input file of parking locations");
        }

        for (int i = 0; i < xCoordList.size(); i++) {
            Coord prCoord = new Coord(xCoordList.get(i), yCoordList.get(i));
            XYCoords.add(prCoord);
        }
        return XYCoords;
    }
}
