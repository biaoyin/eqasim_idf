package org.eqasim.core.simulation.mode_choice.constraints;

import org.matsim.api.core.v01.BasicLocation;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contribs.discrete_mode_choice.components.utils.LocationUtils;
import org.matsim.contribs.discrete_mode_choice.components.utils.home_finder.HomeFinder;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.contribs.discrete_mode_choice.model.tour_based.TourCandidate;
import org.matsim.contribs.discrete_mode_choice.model.tour_based.TourConstraint;
import org.matsim.contribs.discrete_mode_choice.model.tour_based.TourConstraintFactory;
import org.matsim.facilities.Facility;

import java.util.Collection;
import java.util.List;
/** For eqasimVehicleTourConstraint
 * Attention. This constraint generalizes the existing vehicle tour constraint
 * from the discrete_mode_choice contrib of MATSim. See below for the
 * documentation. Eventually, this extended version should be moved to the
 * MATSim contrib.
 *
 * This constraint makes sure that trips are continuous in the sense that
 * vehicles get not dumped somewhere in the network:
 *
 * <ul>
 * <li>Vehicles can only be used where they have been moved to before.</li>
 * <li>Within one tour, vehicles must depart first from the home location.</li>
 * <li>Within one tour, vehicles must be brought back to the home location.</li>
 * </ul>
 *
 * For that, it needs to be decided where "home" is. Currently, there are two
 * options: Either the location of the first activity is used (as it is for
 * SubtourModeChoice), or the location of first activity with a certain type
 * (default is "home") is used.
 *
 * If a home location cannot be found in the tour, a mode must start and end at
 * the first and last location in the tour.
 *
 * @author sebhoerl
 *
 * /** For IntermodalModesConstraint
 *
 *
 *
 * @author azise
 *
 */
public class IntermodalModesConstraint implements TourConstraint{
    private final Collection<String> restrictedModes;
    private final Id<? extends BasicLocation> homeLocationId;
    private final List<Coord> parkRideCoords;
    private final Network network;

    public IntermodalModesConstraint(Collection<String> restrictedModes, Id<? extends BasicLocation> homeLocationId,
                                     List<Coord> parkRideCoords, Network network) {
        this.restrictedModes = restrictedModes;
        this.homeLocationId = homeLocationId;
        this.parkRideCoords = parkRideCoords;
        this.network = network;
    }

    private int getFirstIndex(String mode, List<String> modes) {
        for (int i = 0; i < modes.size(); i++) {
            if (modes.get(i).equals(mode)) {
                return i;
            }
        }

        return -1;
    }

    private int getLastIndex(String mode, List<String> modes) {
        for (int i = modes.size() - 1; i >= 0; i--) {
            if (modes.get(i).equals(mode)) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean validateBeforeEstimation(List<DiscreteModeChoiceTrip> tour, List<String> modes,
                                            List<List<String>> previousModes) {

        boolean found_bike_pt = false;
        boolean found_pt_bike = false;

        // checking car_pt and pt_car in the list of possible modes to be used
        /*
         * for (String mode : modes) { if (mode.equals("pt_car")) { found_pt_car = true;
         * }
         *
         * if (mode.equals("car_pt")) { if (!found_pt_car) { return false; }
         *
         * found_pt_car = false; } }
         */


        for (String mode : modes) {
            if (mode.equals("bike_pt")) {
                found_bike_pt = true;
            }

            if (mode.equals("pt_bike")) {
                if (!found_bike_pt) {
                    return false;
                }

                found_bike_pt = false;
            }
        }

        if (found_bike_pt) {
            return false;
        }

        Id<? extends BasicLocation> latestBikePtOriginId = null;
        Facility prkFacilityOrig = null;
        Facility prkFacilityDest = null;
        Activity intermodalInteractionGoing = null;
        Activity intermodalInteractionComing = null;
        for (int i = 0; i < tour.size(); i++) {

            if (modes.get(i).equals("bike_pt")) {
                latestBikePtOriginId = LocationUtils.getLocationId(tour.get(i).getOriginActivity());

                if(!tour.get(i).getOriginActivity().getType().equals("home")) {
                    return false;
                }



                //ParkingFinder prFinderOri = new ParkingFinder(parkRideCoords);

                //prkFacilityOrig = prFinderOri.getParking(tour.get(i).getOriginActivity().getCoord(), network);

                //Link prLink = NetworkUtils.getNearestLink(network, prkFacilityOri.getCoord());
                //intermodalInteractionGoing = PopulationUtils.createActivityFromCoordAndLinkId(
                //		"intermodal interaction going", prkFacilityOri.getCoord(), prLink.getId());

                // To do parking location constraint
                // if
                // (!latestBikePtOriginId.equals(LocationUtils.getLocationId(intermodalInteraction)))
                // {
                // return false;
                // }

            }

            if (modes.get(i).equals("pt_bike")) {
                Id<? extends BasicLocation> currentLocationId = LocationUtils
                        .getLocationId(tour.get(i).getDestinationActivity());

                // Checking for Origin of car_pt and destination of pt_car
                //if (!latestBikePtOriginId.equals(currentLocationId)) {
                //	return false;
                //}

                if(!tour.get(i).getDestinationActivity().getType().equals("home")){
                    return false;
                }


                // Checking for parking plot location according to the location of the origin
                // activity of car_pt and the destination activitty of pt_car
                //ParkingFinder prFinderDest = new ParkingFinder(parkRideCoords);

                //prkFacilityDest = prFinderDest.getParking(tour.get(i).getDestinationActivity().getCoord(),
                //		network);

                //Link prLink = NetworkUtils.getNearestLink(network, prkFacilityDest.getCoord());

                //intermodalInteractionComing = PopulationUtils.createActivityFromCoordAndLinkId(
                //		"intermodal interaction coming", prkFacilityDest.getCoord(), prLink.getId());

                // To do parking location constraint
                //if (!prkFacilityOrig.getCoord().equals(prkFacilityDest.getCoord())){
                //	return false;
                //}

            }
        }

        return true;
    }

    @Override
    public boolean validateAfterEstimation(List<DiscreteModeChoiceTrip> tour, TourCandidate candidate,
                                           List<TourCandidate> previousCandidates) {
        return true;
    }

    public static class Factory implements TourConstraintFactory {
        private final Collection<String> restrictedModes;
        private final HomeFinder homeFinder;
        private final List<Coord> parkRideCoords;
        private final Network network;

        public Factory(Collection<String> restrictedModes, HomeFinder homeFinder, List<Coord> parkRideCoords,
                       Network network) {
            this.restrictedModes = restrictedModes;
            this.homeFinder = homeFinder;
            this.parkRideCoords = parkRideCoords;
            this.network = network;
        }

        @Override
        public TourConstraint createConstraint(Person person, List<DiscreteModeChoiceTrip> planTrips,
                                               Collection<String> availableModes) {
            return new IntermodalModesConstraint(restrictedModes, homeFinder.getHomeLocationId(planTrips),
                    parkRideCoords, network);
        }
    }

}
