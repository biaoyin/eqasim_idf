package org.eqasim.core.components;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.controler.AbstractModule;

import java.util.ArrayList;
import java.util.List;


public class ParkRideManager extends AbstractModule{

    private static List<Coord> parkRideCoords;
    public ParkRideManager () {
    }

    public void setParkRideCoords(List<Coord> parkRideCoords) {
        this.parkRideCoords = parkRideCoords;
    }

    public List<Coord> getCoordinates() {
         return parkRideCoords;
    }


    @Override
    public void install() {
        // TODO Auto-generated method stub
    }
}
