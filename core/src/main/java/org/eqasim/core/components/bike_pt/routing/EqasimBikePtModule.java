package org.eqasim.core.components.bike_pt.routing;

import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.RoutingModule;

import java.util.Collections;
import java.util.List;

public class EqasimBikePtModule extends AbstractModule{
    List<Coord> parkRideCoords;

    public EqasimBikePtModule(List<Coord> parkRideCoords) {
        this.parkRideCoords = parkRideCoords;
    }


    @Override
    public void install() {
        // TODO Auto-generated method stub
        addRoutingModuleBinding("bike_pt").to(BikePtRoutingModule.class);
    }


    @Provides
    public BikePtRoutingModule provideBikePtRoutingModule(@Named("bike")RoutingModule bikeRoutingModule, @Named("pt")RoutingModule ptRoutingModule, Network network) {
        Network carNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(carNetwork, Collections.singleton("car"));
        return new BikePtRoutingModule(bikeRoutingModule, ptRoutingModule, carNetwork, parkRideCoords);

    }
}
