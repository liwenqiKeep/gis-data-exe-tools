package com.example.utils;

/**
 * Date:2020/11/25
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */

import java.util.Iterator;
import java.util.List;

public class LonLatCoordinate {
    public double lonCoord;
    public double latCoord;
    public static final double MIN_LON = -180.0D;
    public static final double MIN_LAT = -90.0D;
    public static final double MAX_LON = 180.0D;
    public static final double MAX_LAT = 90.0D;

    public LonLatCoordinate() {
    }

    public LonLatCoordinate(double lon, double lat) {
        this.lonCoord = lon;
        this.latCoord = lat;
    }

    public boolean isRightLonLat(boolean isWriteLog) {
        if (-180.0D < this.lonCoord && this.lonCoord < 180.0D && -90.0D < this.latCoord && this.latCoord < 90.0D) {
            return true;
        } else {
            if (isWriteLog) {
            }

            return false;
        }
    }

    public boolean isRightLonLat() {
        return -180.0D < this.lonCoord && this.lonCoord < 180.0D && -90.0D < this.latCoord && this.latCoord < 90.0D;
    }

    public static boolean isRightLonLat(String lonStr, String latStr) {
        double lon = 0.0D;
        double lat = 0.0D;

        try {
            lon = Double.parseDouble(lonStr);
            lat = Double.parseDouble(latStr);
        } catch (NumberFormatException var7) {
            return false;
        }

        return (new LonLatCoordinate(lon, lat)).isRightLonLat();
    }

    public static boolean isRightLonLat(List<LonLatCoordinate> lonlats) {
        Iterator var1 = lonlats.iterator();

        LonLatCoordinate lonlat;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            lonlat = (LonLatCoordinate) var1.next();
        } while (lonlat.isRightLonLat());

        return false;
    }
}
