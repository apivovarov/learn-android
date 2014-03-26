
package org.x4444.app1u;

public class GeoUtils {

    /**
     * <a href="http://www.movable-type.co.uk/scripts/latlong.html">Calculate
     * distance, bearing and more between Latitude/Longitude points</a>
     * 
     * @param lat1D
     * @param lon1D
     * @param lat2D
     * @param lon2D
     * @return distance in meter
     */
    public static double getDistance(double lat1D, double lon1D, double lat2D, double lon2D) {
        // Earth radius m
        double R = 6371000;
        double dLat = Math.toRadians(lat2D - lat1D);
        double dLon = Math.toRadians(lon2D - lon1D);
        double lat1 = Math.toRadians(lat1D);
        double lat2 = Math.toRadians(lat2D);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2)
                * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        return d;
    }
}
