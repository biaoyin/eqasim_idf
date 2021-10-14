package org.eqasim.core.components.bike_pt.routing;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.controler.AbstractModule;

import java.util.ArrayList;
import java.util.List;


public class ParkRideManager extends AbstractModule{

    private static List<Coord> parkRideCoords;
    /*
     @Provides
     @Singleton
     public List<Coord> getCoordinates() {
         // Park and ride lot location
     /*  List<Coord> parkRideCoords = new ArrayList<Coord>();

         double[] xCoord = { 651027.1568424464, 651919.2258891058, 651165.9943205818,  653469.8056880349, 650994.8406433397, 651143.8944379843, 652433.6706034512,
                 650838.9, 651033.5, 651230.3, 653172.6, 652869.1, 654944.1, 654947, 655513.6, 654667.4, 655490.1};

         double[] yCoord = { 6867583.105608011, 6869083.671714151,  6868164.642925204, 6867594.646712293,6869324.290716814, 6869583.91890625, 6868812.527869286,
                 6871040.2, 6870934.6, 6870822.7, 6871088.3, 6871176.6, 6870171, 6869546, 6869799.3, 6868444.9, 6868899.2};

         for (int i = 0; i < yCoord.length; i++) {
             Coord prCoord = new Coord(xCoord[i], yCoord[i]);
             parkRideCoords.add(prCoord);
         }

         return parkRideCoords;
     }
 */
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
