package com.example.nachito.spear;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.List;

import pt.lsts.imc.Maneuver;
import pt.lsts.util.PlanUtilities;

import static com.example.nachito.spear.MainActivity.planWaypoints;

/**
 * Created by ines on 3/7/18.
 */

public class DrawWaypoints {
    private static Maneuver maneuverFromPlan;


    public static void callWaypoint(List<Maneuver> maneuverList) {
        for (int i = 0; i < maneuverList.size(); i++) {
            //para cada Maneuver chamar o waypoints
            wayPoints(maneuverList.get(i));

        }
    }

    private static void wayPoints(final Maneuver maneuver) {
        maneuverFromPlan = maneuver;
        makePoints();
    }


    private static void makePoints() {
        GeoPoint ponto;
        double valueOfLatitude;
        double valueOfLongitude;
        MainActivity.waypointsFromPlan = PlanUtilities.computeWaypoints(maneuverFromPlan);
        for (PlanUtilities.Waypoint point : MainActivity.waypointsFromPlan) {
            valueOfLatitude = point.getLatitude();
            valueOfLongitude = point.getLongitude();
            ponto = new GeoPoint(valueOfLatitude, valueOfLongitude);
            if (!(planWaypoints.contains(ponto))) {
                if (MainActivity.maneuverList.size() != 0) {
                    if (planWaypoints.size() != MainActivity.maneuverList.size()) {
                        planWaypoints.add(ponto);

                    }
                } else if (Area.maneuverArrayList != null) {
                    if (planWaypoints.size() != Area.maneuverArrayList.size()) {
                        planWaypoints.add(ponto);

                    }
                } else if (Line.sendmList() != null) {
                    if (planWaypoints.size() != Line.sendmList().size()) {
                        planWaypoints.add(ponto);
                    }

                }
            }

        }
        MainActivity.planWaypointPolyline.setWidth(5);
        if (planWaypoints.size() != 0)
            MainActivity.planWaypointPolyline.setPoints(planWaypoints);

    }
}
