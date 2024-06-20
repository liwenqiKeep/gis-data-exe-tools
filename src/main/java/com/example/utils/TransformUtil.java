package com.example.utils;

/**
 * Date: 2020/11/26
 * Description: <描述>
 *
 * @Author: Oyoyoyoyoyoyo
 */
public class TransformUtil {
    public TransformUtil() {
    }

    public static boolean outOfChina(double lat, double lng) {
        if (lng >= 72.004D && lng <= 137.8347D) {
            return lat < 0.8293D || lat > 55.8271D;
        } else {
            return true;
        }
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0D + 2.0D * x + 3.0D * y + 0.2D * y * y + 0.1D * x * y + 0.2D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * 3.141592653589793D) + 20.0D * Math.sin(2.0D * x * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(y * 3.141592653589793D) + 40.0D * Math.sin(y / 3.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (160.0D * Math.sin(y / 12.0D * 3.141592653589793D) + 320.0D * Math.sin(y * 3.141592653589793D / 30.0D)) * 2.0D / 3.0D;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0D + x + 2.0D * y + 0.1D * x * x + 0.1D * x * y + 0.1D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * 3.141592653589793D) + 20.0D * Math.sin(2.0D * x * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(x * 3.141592653589793D) + 40.0D * Math.sin(x / 3.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        ret += (150.0D * Math.sin(x / 12.0D * 3.141592653589793D) + 300.0D * Math.sin(x / 30.0D * 3.141592653589793D)) * 2.0D / 3.0D;
        return ret;
    }

    public static double[] delta(double lat, double lng) {
        double[] delta = new double[2];
        double a = 6378137.0D;
        double ee = 0.006693421622965943D;
        double dLat = transformLat(lng - 105.0D, lat - 35.0D);
        double dLng = transformLon(lng - 105.0D, lat - 35.0D);
        double radLat = lat / 180.0D * 3.141592653589793D;
        double magic = Math.sin(radLat);
        magic = 1.0D - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        delta[0] = dLat * 180.0D / (a * (1.0D - ee) / (magic * sqrtMagic) * 3.141592653589793D);
        delta[1] = dLng * 180.0D / (a / sqrtMagic * Math.cos(radLat) * 3.141592653589793D);
        return delta;
    }
}