package org.eqasim.ile_de_france.intermodality.swiss_railway_raptor;

import ch.sbb.matsim.routing.pt.raptor.IntermodalAwareRouterModeIdentifier;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.TripStructureUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Biao Yin
 */
public class IDFIntermodalAwareRouterModeIdentifier extends IntermodalAwareRouterModeIdentifier {

    private final Set<String> transitModes;

    @Inject
    public IDFIntermodalAwareRouterModeIdentifier(Config config) {
        super(config);
        this.transitModes = config.transit().getTransitModes();
    }

    /** Intermodal trips can have a number of different legs and interaction activities, e.g.:
     * non_network_walk | bike-interaction | bike | pt-interaction | transit-walk | pt-interaction | train | pt-interaction | non_network_walk
     * Thus, this main mode identifier uses the following heuristic to decide to which router mode a trip belongs:
     * - if there is a leg with a pt mode (based on config.transit().getTransitModes(), it returns that pt mode.
     * - if there is only a leg with mode transit_walk, one of the configured transit modes is returned.
     * - otherwise, the first mode not being an non_network_walk or transit_walk.
     *
     * The above comment is a little outdated since we introduced routing mode. However, with routing mode this MainModeIdentifier
     * will no longer be used except for backward compatibility, i.e. update old plans to the new format adding the attribute
     * routing mode. -gl nov'19
     */
    @Override
    public String identifyMainMode(List<? extends PlanElement> tripElements) {

      /*  //Biao: bike_pt mode
        for (Activity act : TripStructureUtils.getActivities(tripElements, TripStructureUtils.StageActivityHandling.StagesAsNormalActivities)) {
            if(TripStructureUtils.isStageActivityType(act.getType())){

                if (act.getType().equals("bikePt interaction") || act.getType().equals("ptBike interaction")) {
                    return "bike_pt";
                }

            }

        }*/

        for (Leg leg : TripStructureUtils.getLegs(tripElements)) {
            if (!leg.getMode().contains("walk")) {
                return leg.getMode();
            }
        }

        String singleLegMode = TripStructureUtils.getLegs(tripElements).get(0).getMode();

        switch (singleLegMode) {
            case TransportMode.transit_walk:
            case TransportMode.non_network_walk:
                return TransportMode.pt;
            default:
                return TransportMode.walk;
        }
    }
}
