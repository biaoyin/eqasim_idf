package org.eqasim.ile_de_france.mode_choice.utilities.predictors;

import com.google.inject.Inject;
import org.eqasim.core.simulation.mode_choice.utilities.predictors.CachedVariablePredictor;
import org.eqasim.core.tools.TestCarPtPara;
import org.eqasim.ile_de_france.mode_choice.parameters.TestTollFee;
import org.eqasim.ile_de_france.mode_choice.utilities.variables.IDFCarRoadPricingVariables;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.contribs.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IDFCarRoadPricingPredictor extends CachedVariablePredictor<IDFCarRoadPricingVariables> {

    private final Network network;
    private final PopulationFactory populationFactory;
    //private final ModeParameters parameters;

    @Inject
    public IDFCarRoadPricingPredictor(Network network, PopulationFactory populationFactory) {

        this.network = network;
        this.populationFactory = populationFactory;
    }

    @Override
    public IDFCarRoadPricingVariables predict(Person person, DiscreteModeChoiceTrip trip, List<? extends PlanElement> elements) {
        // read shapefile of the study area

        final String areaShapeFile = TestTollFee.getTollAreaFilePath();

        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(areaShapeFile);

        Map<String, Geometry> zoneGeometries = new HashMap<>();
        for (SimpleFeature feature : features) {
            zoneGeometries.put((String)feature.getAttribute("Class"),(Geometry)feature.getDefaultGeometry());
        }
        Geometry polygon_paris_ville = zoneGeometries.get(("paris_inner"));
        //BoundingBox bb_paris_ville = paris.getBounds();

        final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory();
        //GeometryFactory gf = new GeometryFactory();

        Coord toolPoint = new Coord();

        double fee_toll = 0.0;
        final double fee_fixed = TestTollFee.getTollFee();

        final Coordinate coordOrigin = CoordUtils.createGeotoolsCoordinate(trip.getOriginActivity().getCoord());

        final Coordinate coordDestination = CoordUtils.createGeotoolsCoordinate(trip.getDestinationActivity().getCoord());

        Geometry pointOrigin = gf.createPoint(new Coordinate(coordOrigin.x,coordOrigin.y));

        Geometry pointDestination = gf.createPoint(new Coordinate(coordDestination.x,coordDestination.y));

        //TO DO: Add the time of the road pricing: 5h30-9h (330 - 540) and 15h30-19h (930 - 1140) according to the arrival time computed by using:
        Leg leg = (Leg) elements.get(0);

        double travelTime_min = leg.getTravelTime().seconds() / 60;

        //cas 1 application du road pricing zonale (aire) entre 5h-9h
        //if (travelTime_min >= 330 && travelTime_min <= 540){

            //Origine/Destination dans la ville de Lille
         if(polygon_paris_ville.contains(pointOrigin) || polygon_paris_ville.contains(pointDestination)){
            toolPoint = trip.getDestinationActivity().getCoord();

            // This activity is not added in the plan.  BYIN 2023-04-29
            Activity toll_activity = (Activity) PopulationUtils.createActivityFromCoord("toll interaction",
                    toolPoint);
            toll_activity.setMaximumDuration(10);// 10 s

            fee_toll = fee_fixed;// euros
         }
        //}

        return new IDFCarRoadPricingVariables(fee_toll);
    }
}
