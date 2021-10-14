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

public class EqasimPtBikeModule extends AbstractModule{
    List<Coord> parkRideCoords;

    public EqasimPtBikeModule(List<Coord> parkRideCoords) {
        this.parkRideCoords = parkRideCoords;
    }


    @Override
    public void install() {
        // TODO Auto-generated method stub
        addRoutingModuleBinding("pt_bike").to(PtBikeRoutingModule.class);
    }


    @Provides
    public PtBikeRoutingModule providePtCarRoutingModule(@Named("pt")RoutingModule ptRoutingModule, @Named("bike")RoutingModule bikeRoutingModule, Network network) {
        Network carNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(carNetwork, Collections.singleton("car"));
        return new PtBikeRoutingModule(ptRoutingModule, bikeRoutingModule, carNetwork, parkRideCoords);

    }
}
