package org.eqasim.ile_de_france.driving_restriction;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class LinksInArea {
    private static final Logger LOG = Logger.getLogger(LinksInArea.class);

    public static void main (String[] args) throws IOException {
        // Input and output files
        String networkInputFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\ile_de_france_1pm\\matsim_input\\ile_de_france_network.xml.gz";
        String linkIDOutputFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\scenarios\\mobility_model\\link_specification_Paris\\Paris_linkIDs.txt";

        String areaShapeFile = "C:\\Users\\biao.yin\\Documents\\MATSIM\\Project\\qgis\\Paris\\paris.shp";

        // Get network
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        MatsimNetworkReader reader = new MatsimNetworkReader(scenario.getNetwork());
        reader.readFile(networkInputFile);

        // Store relevant area of city as geometry
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(areaShapeFile);
        Map<String, Geometry> zoneGeometries = new HashMap<>();
        for (SimpleFeature feature : features) {
            zoneGeometries.put((String) feature.getAttribute("scenario"), (Geometry) feature.getDefaultGeometry());
        }
        Geometry areaGeometry = zoneGeometries.get("paris");
        // Collect all links that within the area
        Set<Id<Link>> retainedLinkIds = new HashSet<>();

        for (Link link : scenario.getNetwork().getLinks().values()) {
            Point linkCenterAsPoint = MGC.xy2Point(link.getCoord().getX(), link.getCoord().getY());
            if (!areaGeometry.contains(linkCenterAsPoint)) {
                scenario.getNetwork().removeLink(link.getId());
            }

            if (areaGeometry.contains(linkCenterAsPoint)) {
                retainedLinkIds.add(link.getId());
            }


        }

        // Write modified network to file
//        NetworkWriter writer = new NetworkWriter(scenario.getNetwork());
//        writer.write(networkOutputFile);
        // Write linkIDs to file
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(linkIDOutputFile));
        Iterator it = retainedLinkIds.iterator();
        while (it.hasNext()) {
            fileWriter.write(it.next().toString());
            fileWriter.newLine();
        }
        fileWriter.close();

    }
}
