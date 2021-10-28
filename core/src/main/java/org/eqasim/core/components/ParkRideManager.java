package org.eqasim.core.components;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.controler.AbstractModule;

import java.util.ArrayList;
import java.util.List;


public class ParkRideManager extends AbstractModule{

    private static List<Coord> parkRideCoords;
    private static Network network;

    public ParkRideManager () {
    }

    public void setParkRideCoords(List<Coord> parkRideCoords) {
        this.parkRideCoords = parkRideCoords;
    }
    public void setNetwork(Network network) {
        this.network = network;
    }

    public static List<Coord> getCoordinates() {
         return parkRideCoords;
    }
    public static Network getNetwork() {return network;}

    @Override
    public void install() {
        // TODO Auto-generated method stub
    }
}
