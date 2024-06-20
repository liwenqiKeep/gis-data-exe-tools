package com.example.utils;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.io.WKBWriter;

import java.util.HashMap;

/**
 * Date:2020/11/25
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */
public class GridCoordinateGetter {
    private static HashMap<Integer, EarthDivisionData> gridDivisionMap = null;
    private static Object lock = new Object();

    public GridCoordinateGetter() {
    }

    private static HashMap<Integer, EarthDivisionData> getGridDivisionMap() {
        if (gridDivisionMap != null) {
            return gridDivisionMap;
        } else {
            synchronized (lock) {
                EarthDivisionDataLoader divDataLoader = new EarthDivisionDataLoader();
                HashMap<Integer, EarthDivisionData> divisionDataMap = divDataLoader.loadEarthDivisionData("EarthDivisionData.csv");
                gridDivisionMap = divisionDataMap;
            }

            return gridDivisionMap;
        }
    }

    public static GridOffSetData getGridOffset(double longitude, double latitude, double gSize) {
        HashMap<Integer, EarthDivisionData> earthData = getGridDivisionMap();
        GridOffSetData offSetData = new GridOffSetData(0, 0, 0);
        if (longitude >= -180.0D && latitude >= -90.0D) {
            int lonZoneNum = (int) ((longitude + 180.0D) / 6.0D + 1.0D) * 100;
            int latZoneNum = (int) ((latitude + 90.0D) / 3.0D) + 1;
            if (longitude < earthData.get(lonZoneNum + latZoneNum).realLeftLon) {
                if (lonZoneNum == 100) {
                    lonZoneNum = 6000;
                } else {
                    lonZoneNum -= 100;
                }
            }

            offSetData.earthID = lonZoneNum + latZoneNum;
            double meridian = earthData.get(offSetData.earthID).centerLon;
            double gaussX = lonLat2X(longitude, latitude, meridian);
            double disCurPointtocenterX = gaussX - ((EarthDivisionData) earthData.get(offSetData.earthID)).centerX;
            offSetData.xOffSet = (int) (disCurPointtocenterX / gSize);
            offSetData.xOffSet = disCurPointtocenterX >= 0.0D ? offSetData.xOffSet + 1 : offSetData.xOffSet - 1;
            double gaussY = lonLat2Y(longitude, latitude, meridian);
            double disCurPointtocenterY = gaussY - ((EarthDivisionData) earthData.get(offSetData.earthID)).centerY;
            offSetData.yOffSet = (int) (disCurPointtocenterY / gSize);
            offSetData.yOffSet = disCurPointtocenterY >= 0.0D ? offSetData.yOffSet + 1 : offSetData.yOffSet - 1;
            return offSetData;
        } else {
            return offSetData;
        }
    }

    public static int getEarthID(double longitude, double latitude, HashMap<Integer, EarthDivisionData> earthData) {
        int eID = 0;
        if (longitude >= -180.0D && latitude >= -90.0D) {
            int lonZoneNum = (int) ((longitude + 180.0D) / 6.0D + 1.0D) * 100;
            int latZoneNum = (int) ((latitude + 90.0D) / 3.0D) + 1;
            if (longitude < ((EarthDivisionData) earthData.get(lonZoneNum + latZoneNum)).realLeftLon) {
                if (lonZoneNum == 100) {
                    lonZoneNum = 6000;
                } else {
                    lonZoneNum -= 100;
                }
            }

            eID = lonZoneNum + latZoneNum;
            return eID;
        } else {
            return eID;
        }
    }

    public static int getXOffSet(double longitude, double latitude, double gSize, HashMap<Integer, EarthDivisionData> earthData, int ethID) {
        double meridian = ((EarthDivisionData) earthData.get(ethID)).centerLon;
        double gaussX = lonLat2X(longitude, latitude, meridian);
        double disCurPointtocenterX = gaussX - ((EarthDivisionData) earthData.get(ethID)).centerX;
        int xOffSet = (int) (disCurPointtocenterX / gSize);
        xOffSet = disCurPointtocenterX >= 0.0D ? xOffSet + 1 : xOffSet - 1;
        return xOffSet;
    }

    public static int getYOffSet(double longitude, double latitude, double gSize, HashMap<Integer, EarthDivisionData> earthData, int ethID) {
        double meridian = ((EarthDivisionData) earthData.get(ethID)).centerLon;
        double gaussY = lonLat2Y(longitude, latitude, meridian);
        double disCurPointtocenterY = gaussY - ((EarthDivisionData) earthData.get(ethID)).centerY;
        int yOffSet = (int) (disCurPointtocenterY / gSize);
        yOffSet = disCurPointtocenterY >= 0.0D ? yOffSet + 1 : yOffSet - 1;
        return yOffSet;
    }

    //public static Geometry getGridGeometry(GridOffSetData offSetData, double gSize, GeometryFactory factory) {
    //    HashMap<Integer, EarthDivisionData> divisionMap = getGridDivisionMap();
    //    if (!divisionMap.containsKey(offSetData.earthID)) {
    //        return null;
    //    } else {
    //        double X = 0.0D;
    //        double Y = 0.0D;
    //        double changeGridSizeX = gSize;
    //        double changeGridSizeY = gSize;
    //        if (offSetData.xOffSet > 0) {
    //            changeGridSizeX = -gSize;
    //        }
    //
    //        if (offSetData.yOffSet > 0) {
    //            changeGridSizeY = -gSize;
    //        }
    //
    //        X = (double) offSetData.xOffSet * gSize + 500000.0D;
    //        Y = (double) offSetData.yOffSet * gSize + ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerY;
    //        LonLatCoordinate topleftLonLat = projectToLonLat(X, Y, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
    //        LonLatCoordinate toprightLonLat = projectToLonLat(X + changeGridSizeX, Y, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
    //        LonLatCoordinate rightbottomLonLat = projectToLonLat(X + changeGridSizeX, Y + changeGridSizeY, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
    //        LonLatCoordinate leftbotomLonLat = projectToLonLat(X, Y + changeGridSizeY, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
    //        Coordinate topleft = new Coordinate(topleftLonLat.lonCoord, topleftLonLat.latCoord);
    //        Coordinate topright = new Coordinate(toprightLonLat.lonCoord, toprightLonLat.latCoord);
    //        Coordinate rightbottom = new Coordinate(rightbottomLonLat.lonCoord, rightbottomLonLat.latCoord);
    //        Coordinate leftbotom = new Coordinate(leftbotomLonLat.lonCoord, leftbotomLonLat.latCoord);
    //        Coordinate[] coords = new Coordinate[]{topleft, topright, rightbottom, leftbotom, topleft};
    //        return factory.createPolygon(factory.createLinearRing(coords), (LinearRing[]) null);
    //    }
    //}

    public static Geometry getGridGeometry(GridOffSetData offSetData, double gSize, GeometryFactory factory) {
        HashMap<Integer, EarthDivisionData> divisionMap = getGridDivisionMap();
        if (!divisionMap.containsKey(offSetData.earthID)) {
            return null;
        } else {
            double X = 0.0D;
            double Y = 0.0D;
            double changeGridSizeX = gSize;
            double changeGridSizeY = gSize;
            if (offSetData.xOffSet > 0) {
                changeGridSizeX = -gSize;
            }

            if (offSetData.yOffSet > 0) {
                changeGridSizeY = -gSize;
            }

            X = (double) offSetData.xOffSet * gSize + 500000.0D;
            Y = (double) offSetData.yOffSet * gSize + ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerY;
            LonLatCoordinate topleftLonLat = projectToLonLat(X, Y, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
            LonLatCoordinate toprightLonLat = projectToLonLat(X + changeGridSizeX, Y, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
            LonLatCoordinate rightbottomLonLat = projectToLonLat(X + changeGridSizeX, Y + changeGridSizeY, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
            LonLatCoordinate leftbotomLonLat = projectToLonLat(X, Y + changeGridSizeY, ((EarthDivisionData) divisionMap.get(offSetData.earthID)).centerLon);
            Coordinate topleft = new Coordinate(topleftLonLat.lonCoord, topleftLonLat.latCoord);
            Coordinate topright = new Coordinate(toprightLonLat.lonCoord, toprightLonLat.latCoord);
            Coordinate rightbottom = new Coordinate(rightbottomLonLat.lonCoord, rightbottomLonLat.latCoord);
            Coordinate leftbotom = new Coordinate(leftbotomLonLat.lonCoord, leftbotomLonLat.latCoord);
            Coordinate[] coords = new Coordinate[]{topleft, topright, rightbottom, leftbotom, topleft};
            return factory.createPolygon(factory.createLinearRing(coords), (LinearRing[]) null);
        }
    }

    // private String outFormat(WKBWriter wkbWriter, String fid, GridOffSetData gridOffset, Geometry geometry, String fidAttributeName, SimpleFeature feature) {
    public static String outFormat(WKBWriter wkbWriter, GridOffSetData gridOffset, Geometry geometry, String fidAttributeName) {

        String[] headers = fidAttributeName.split(",");
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < headers.length; ++i) {
            if (i != 0) {
                stringBuilder.append(",");
            }

            String header = headers[i];
            if (header.equalsIgnoreCase("earthId")) {
                stringBuilder.append(gridOffset.earthID);
            } else if (header.equalsIgnoreCase("xoffset")) {
                stringBuilder.append(gridOffset.xOffSet);
            } else if (header.equalsIgnoreCase("yoffset")) {
                stringBuilder.append(gridOffset.yOffSet);
            } else if (header.equalsIgnoreCase("geom")) {
                stringBuilder.append(WKBWriter.toHex(wkbWriter.write(geometry)));
            }
            // else {
            //     Object attribute = feature.getAttribute(header);
            //     if (attribute == null) {
            //         attribute = fid;
            //     }
            //
            //     stringBuilder.append(attribute.toString());
            // }
        }

        return stringBuilder.toString();
    }

    public static double lonLat2X(double lon, double lat, double merid) {
        double meridianSecond = 0.0D;
        double longAxis = 6378137.0D;
        double shortAxis = 6356752.314245D;
        meridianSecond = merid * 3600.0D;
        double lonSecond = lon * 3600.0D;
        double latSecond = lat * 3600.0D;
        double e1 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (longAxis * longAxis));
        double e2 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (shortAxis * shortAxis));
        double gaussX = longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 206264.808D * Math.cos(latSecond / 206264.808D) * (lonSecond - meridianSecond) + longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 6.0D / Math.pow(206264.808D, 3.0D) * Math.pow(Math.cos(latSecond / 206264.808D), 3.0D) * (1.0D - Math.pow(Math.tan(latSecond / 206264.808D), 2.0D) + e2 * e2 * Math.pow(Math.cos(latSecond / 206264.808D), 2.0D)) * Math.pow(lonSecond - meridianSecond, 3.0D) + longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 120.0D / Math.pow(206264.808D, 5.0D) * Math.pow(Math.cos(latSecond / 206264.808D), 5.0D) * (5.0D - 18.0D * Math.pow(Math.tan(latSecond / 206264.808D), 2.0D) + Math.pow(Math.tan(latSecond / 206264.808D), 4.0D) + 14.0D * e2 * e2 * Math.pow(Math.cos(latSecond / 206264.808D), 2.0D) - 58.0D * e2 * e2 * Math.pow(Math.cos(latSecond / 206264.808D), 2.0D) * Math.pow(Math.tan(latSecond / 206264.808D), 2.0D)) * Math.pow(lonSecond - meridianSecond, 5.0D) + 500000.0D;
        return gaussX;
    }

    public static double lonLat2Y(double lon, double lat, double merid) {
        double meridianSecond = 0.0D;
        double longAxis = 6378137.0D;
        double shortAxis = 6356752.314245D;
        meridianSecond = merid * 3600.0D;
        double lonSecond = lon * 3600.0D;
        double latSecond = lat * 3600.0D;
        double lengthFromLat0 = getLengthFromLatO(longAxis, shortAxis, lat);
        double e1 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (longAxis * longAxis));
        double e2 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (shortAxis * shortAxis));
        double gaussY = lengthFromLat0 + longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 2.0D / Math.pow(206264.808D, 2.0D) * Math.sin(latSecond / 206264.808D) * Math.cos(latSecond / 206264.808D) * Math.pow(lonSecond - meridianSecond, 2.0D) + longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 24.0D / Math.pow(206264.808D, 4.0D) * Math.sin(latSecond / 206264.808D) * Math.pow(Math.cos(latSecond / 206264.808D), 3.0D) * (5.0D - Math.pow(Math.tan(latSecond / 206264.808D), 2.0D) + 9.0D * e2 * e2 * Math.pow(Math.cos(latSecond / 206264.808D), 2.0D) + 4.0D * Math.pow(e2 * e2 * Math.pow(Math.cos(latSecond / 206264.808D), 2.0D), 2.0D)) * Math.pow(lonSecond - meridianSecond, 4.0D) + longAxis / Math.pow(1.0D - e1 * e1 * Math.pow(Math.sin(latSecond / 206264.808D), 2.0D), 0.5D) / 720.0D / Math.pow(206264.808D, 6.0D) * Math.sin(latSecond / 206264.808D) * Math.pow(Math.cos(latSecond / 206264.808D), 5.0D) * (61.0D - 58.0D * Math.pow(Math.tan(latSecond / 206264.808D), 2.0D) + Math.pow(Math.tan(latSecond / 206264.808D), 4.0D)) * Math.pow(lonSecond - meridianSecond, 6.0D);
        return gaussY;
    }

    public static double getLengthFromLatO(double lAxis, double sAxis, double lat) {
        double d = sAxis * sAxis / lAxis;
        double e = Math.sqrt((lAxis * lAxis - sAxis * sAxis) / (lAxis * lAxis));
        double a0 = d * (1.0D + 0.75D * e * e + 0.703125D * e * e * e * e + 0.68359375D * e * e * e * e * e * e + 0.67291259765625D * e * e * e * e * e * e * e * e);
        double b0 = d * (0.75D * e * e + 0.703125D * e * e * e * e + 0.68359375D * e * e * e * e * e * e + 0.67291259765625D * e * e * e * e * e * e * e * e);
        double c0 = d * (0.46875D * e * e * e * e + 0.47554347826086957D * e * e * e * e * e * e + 0.4486083984375D * e * e * e * e * e * e * e * e);
        double d0 = d * (0.3645833333333333D * e * e * e * e * e * e + 0.35888671875D * e * e * e * e * e * e * e * e);
        double e0 = d * 0.3076171875D * e * e * e * e * e * e * e * e;
        double latRad = lat * 3.141592653589793D / 180.0D;
        double length = a0 * lat * 3.141592653589793D / 180.0D - b0 * Math.sin(latRad) * Math.cos(latRad) - c0 * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.cos(latRad) - d0 * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.cos(latRad) - e0 * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.sin(latRad) * Math.cos(latRad);
        return length;
    }

    public static LonLatCoordinate projectToLonLat(double X, double Y, double meridian) {
        LonLatCoordinate lonLat = new LonLatCoordinate();
        ellipsoidType ellType = ellipsoidType.etWGS84E;
        projectionType proType = projectionType.ptGaussKrugerE;
        double longAxis;
        double shortAxis;
        if (ellType == ellipsoidType.etWGS84E) {
            longAxis = 6378137.0D;
            shortAxis = 6356752.314245D;
        } else if (ellType == ellipsoidType.etKrasovsky40E) {
            longAxis = 6378245.0D;
            shortAxis = 6356863.01877305D;
        } else {
            longAxis = 6378245.0D;
            shortAxis = 6356863.01877305D;
        }

        double e1 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (longAxis * longAxis));
        double e2 = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (shortAxis * shortAxis));
        if (proType == projectionType.ptUTME) {
            X -= 500000.0D;
            X = X / 0.9996D + 500000.0D;
            Y /= 0.9996D;
        }

        double Bf = getBfWhenProjectToLonLat(longAxis, shortAxis, X, Y);
        X -= 500000.0D;
        double lat = Bf - Math.tan(Bf) * Math.pow(X, 2.0D) / (2.0D * longAxis * (1.0D - e1 * e1) / Math.pow(Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 3.0D) * longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf))) + Math.tan(Bf) * (5.0D + 3.0D * Math.pow(Math.tan(Bf), 2.0D) + Math.pow(e2 * Math.cos(Bf), 2.0D) - 9.0D * Math.pow(Math.tan(Bf) * e2 * Math.cos(Bf), 2.0D)) * Math.pow(X, 4.0D) / (1.53075288E8D * (1.0D - e1 * e1) / Math.pow(Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 3.0D) * Math.pow(longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 3.0D)) - Math.tan(Bf) * (61.0D + 90.0D * Math.tan(Bf) * Math.tan(Bf) + 45.0D * Math.pow(Math.tan(Bf), 4.0D)) * Math.pow(X, 6.0D) / (720.0D * longAxis * (1.0D - e1 * e1) / Math.pow(Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 3.0D) * Math.pow(longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 5.0D));
        double lon = X / (longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf))) / Math.cos(Bf) - (1.0D + 2.0D * Math.pow(Math.tan(Bf), 2.0D) + Math.pow(e2 * Math.cos(Bf), 2.0D)) * Math.pow(X, 3.0D) / (6.0D * Math.pow(longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 3.0D) * Math.cos(Bf)) + (5.0D + 28.0D * Math.pow(Math.tan(Bf), 2.0D) + 24.0D * Math.pow(Math.tan(Bf), 4.0D) + 6.0D * Math.pow(e2 * Math.cos(Bf), 2.0D) + 8.0D * Math.pow(Math.tan(Bf) * e2 * Math.cos(Bf), 2.0D)) * Math.pow(X, 5.0D) / 120.0D / Math.pow(longAxis / Math.sqrt(1.0D - e1 * e1 * Math.sin(Bf) * Math.sin(Bf)), 5.0D) / Math.cos(Bf);
        lat = lat * 180.0D / 3.141592653589793D;
        lon = lon * 180.0D / 3.141592653589793D + meridian;
        lonLat.latCoord = lat;
        lonLat.lonCoord = lon;
        return lonLat;
    }

    public static double getBfWhenProjectToLonLat(double longAxis, double shortAxis, double X, double Y) {
        double e = Math.sqrt((longAxis * longAxis - shortAxis * shortAxis) / (longAxis * longAxis));
        double e1_2 = e * e;
        double e1_4 = e1_2 * e1_2;
        double e1_6 = e1_4 * e1_2;
        double e1_8 = e1_4 * e1_4;
        double e1_10 = e1_2 * e1_8;
        double aa = 1.0D + 3.0D * e1_2 / 4.0D + 45.0D * e1_4 / 64.0D + 175.0D * e1_6 / 256.0D + 11025.0D * e1_8 / 16384.0D + 43659.0D * e1_10 / 65536.0D;
        double bb = 3.0D * e1_2 / 4.0D + 15.0D * e1_4 / 16.0D + 525.0D * e1_6 / 512.0D + 2205.0D * e1_8 / 2048.0D + 72765.0D * e1_10 / 65536.0D;
        double cc = 15.0D * e1_4 / 64.0D + 105.0D * e1_6 / 256.0D + 2205.0D * e1_8 / 4096.0D + 10395.0D * e1_10 / 16384.0D;
        double dd = 35.0D * e1_6 / 512.0D + 315.0D * e1_8 / 2048.0D + 31185.0D * e1_10 / 13072.0D;
        double a1 = aa * longAxis * (1.0D - e * e);
        double a2 = -bb * longAxis * (1.0D - e * e) / 2.0D;
        double a3 = cc * longAxis * (1.0D - e * e) / 4.0D;
        double a4 = -dd * longAxis * (1.0D - e * e) / 6.0D;
        double[] b11 = new double[5];
        double[] r11 = new double[5];
        double[] d11 = new double[5];
        b11[0] = -a2 / a1;
        r11[0] = -a3 / a1;
        d11[0] = -a4 / a1;

        for (int i = 0; i < 4; ++i) {
            b11[i + 1] = b11[0] + b11[0] * r11[i] - 2.0D * r11[0] * b11[i] - 3.0D * b11[0] * b11[i] * b11[i] / 2.0D;
            r11[i + 1] = r11[0] + b11[0] * b11[i];
            d11[i + 1] = d11[0] + b11[0] * r11[i] + 2.0D * r11[0] * b11[i] + b11[0] * b11[i] * b11[i] / 2.0D;
        }

        double K1 = 2.0D * b11[4] + 4.0D * r11[4] + 6.0D * d11[4];
        double K2 = -8.0D * r11[4] - 32.0D * d11[4];
        double K3 = 32.0D * d11[4];
        double Bf = Y / a1;
        Bf += Math.cos(Bf) * (Math.sin(Bf) * K1 + Math.sin(Bf) * Math.sin(Bf) * Math.sin(Bf) * K2 + Math.sin(Bf) * Math.sin(Bf) * Math.sin(Bf) * Math.sin(Bf) * Math.sin(Bf) * K3);
        return Bf;
    }

    public static enum ellipsoidType {
        etKrasovsky40E,
        etWGS84E;

        private ellipsoidType() {
        }
    }

    public static enum projectionType {
        ptAMGE,
        ptCassiniE,
        ptNewZealandE,
        ptUTME,
        ptUKE,
        ptLambertIIE,
        ptSwedenE,
        ptBonneE,
        ptStereographicE,
        ptGaussKrugerE,
        ptVGISSAE,
        ptEOVE,
        ptLambertConformalE,
        ptMalayanRectifiedSkewE;

        private projectionType() {
        }
    }
}
