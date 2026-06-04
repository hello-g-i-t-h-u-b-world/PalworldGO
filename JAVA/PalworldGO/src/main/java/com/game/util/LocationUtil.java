package com.game.util;

public class LocationUtil {

    public static double[] getRandomPosition(double lat, double lng, double radius) {

        double radiusInDegrees = radius / 111300.0;

        double u = Math.random();
        double v = Math.random();

        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;

        double newLat = lat + w * Math.cos(t);
        double newLng = lng + w * Math.sin(t);

        return new double[]{newLat, newLng};
    }
}