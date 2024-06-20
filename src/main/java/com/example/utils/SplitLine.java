package com.example.utils;

import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.*;

/**
 * Date: 2020/12/16
 * Description: <描述>
 *
 * @Author: Oyoyoyoyoyoyo
 */
public class SplitLine {
    public SplitLine() {
    }


    public static List<LineString> split2Segments(Geometry geometry, double segmentLength) throws Exception {
        List<LineString> segments = new ArrayList<>();
        split2Segments(geometry, segmentLength, segments);
        return segments;
    }

    private static void split2Segments(Geometry geometry, double segmentLength, List<LineString> segments) throws Exception {
        if (geometry instanceof MultiLineString) {
            split2Segments((MultiLineString) geometry, segmentLength, segments);
        } else if (geometry instanceof LineString) {
            split2Segments((LineString) geometry, segmentLength, segments);
        } else {
            throw new Exception("Geometry type not support " + geometry.getClass().getName() + " only support lineString type.");
        }

    }

    private static void split2Segments(MultiLineString multiLineString, double segmentLength, List<LineString> segments) throws Exception {
        int numGeometries = multiLineString.getNumGeometries();

        for (int i = 0; i < numGeometries; ++i) {
            Geometry geometryN = multiLineString.getGeometryN(i);
            split2Segments(geometryN, segmentLength, segments);
        }

    }

    private static void split2Segments(LineString line, double segmentLength, List<LineString> segments) throws Exception {
        CoordinateReferenceSystem sourceCrs = CRS.parseWKT("GEOGCS[\"WGS 84\", \n  DATUM[\"World Geodetic System 1984\", \n    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n    AUTHORITY[\"EPSG\",\"6326\"]], \n  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n  UNIT[\"degree\", 0.017453292519943295], \n  AXIS[\"Geodetic longitude\", EAST], \n  AXIS[\"Geodetic latitude\", NORTH], \n  AUTHORITY[\"EPSG\",\"4326\"]]");
        GeodeticCalculator calculator = new GeodeticCalculator(sourceCrs);
        GeometryFactory geometryFactory = line.getFactory();
        LinkedList<Coordinate> coordinates = new LinkedList<>();
        Collections.addAll(coordinates, line.getCoordinates());
        double accumulatedLength = 0.0D;
        List<Coordinate> lastSegment = new ArrayList<>();
        Iterator<Coordinate> iterator = coordinates.iterator();

        for (int i = 0; iterator.hasNext() && i < coordinates.size() - 1; ++i) {
            Coordinate c1 = coordinates.get(i);
            Coordinate c2 = coordinates.get(i + 1);
            lastSegment.add(c1);
            calculator.setStartingGeographicPoint(c1.x, c1.y);
            calculator.setDestinationGeographicPoint(c2.x, c2.y);
            double length = calculator.getOrthodromicDistance();
            if (length + accumulatedLength >= segmentLength) {
                double offsetLength = segmentLength - accumulatedLength;
                double ratio = offsetLength / length;
                double dx = c2.x - c1.x;
                double dy = c2.y - c1.y;
                Coordinate segmentationPoint = new Coordinate(c1.x + dx * ratio, c1.y + dy * ratio);
                lastSegment.add(segmentationPoint);
                segments.add(geometryFactory.createLineString(lastSegment.toArray(new Coordinate[0])));
                lastSegment = new ArrayList<>();
                accumulatedLength = 0.0D;
                coordinates.add(i + 1, segmentationPoint);
            } else {
                accumulatedLength += length;
            }
        }

        lastSegment.add(coordinates.getLast());
        segments.add(geometryFactory.createLineString(lastSegment.toArray(new Coordinate[0])));
    }
}

