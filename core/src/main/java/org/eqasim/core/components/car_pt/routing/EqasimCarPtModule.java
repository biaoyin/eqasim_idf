package org.eqasim.core.components.car_pt.routing;

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

public class EqasimCarPtModule extends AbstractModule{
    List<Coord> parkRideCoords;

    public EqasimCarPtModule(List<Coord> parkRideCoords) {
        this.parkRideCoords = parkRideCoords;
    }


    @Override
    public void install() {
        // TODO Auto-generated method stub
        addRoutingModuleBinding("car_pt").to(CarPtRoutingModule.class);
    }


    @Provides
    public CarPtRoutingModule provideCarPtRoutingModule(@Named("car")RoutingModule roadRoutingModule, @Named("pt")RoutingModule ptRoutingModule, Network network) {
        Network carNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(carNetwork, Collections.singleton("car"));
        return new CarPtRoutingModule(roadRoutingModule, ptRoutingModule, carNetwork, parkRideCoords);

    }
}
