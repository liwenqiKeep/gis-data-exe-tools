package com.example.utils;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryFixer;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.util.HashSet;

public class GridUtils {

    public static HashSet<GridOffSetData> gridRecords(Geometry geometry, double gridSize, double bufferInMetre) {



        final double bufferdegrees = GeomCommonUtils.mToDegrees(bufferInMetre);

        HashSet<GridOffSetData> gridSet = new HashSet<>();
        Geometry geom;
        try {
            CoordinateReferenceSystem sourceCrs = CRS.parseWKT("GEOGCS[\"WGS 84\", \n  DATUM[\"World Geodetic System 1984\", \n    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n    AUTHORITY[\"EPSG\",\"6326\"]], \n  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n  UNIT[\"degree\", 0.017453292519943295], \n  AXIS[\"Geodetic longitude\", EAST], \n  AXIS[\"Geodetic latitude\", NORTH], \n  AUTHORITY[\"EPSG\",\"4326\"]]");
            CoordinateReferenceSystem targetCrs = CRS.parseWKT("PROJCS[\"WGS 84 / Pseudo-Mercator\", \n  GEOGCS[\"WGS 84\", \n    DATUM[\"World Geodetic System 1984\", \n      SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n      AUTHORITY[\"EPSG\",\"6326\"]], \n    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n    UNIT[\"degree\", 0.017453292519943295], \n    AXIS[\"Geodetic longitude\", EAST], \n    AXIS[\"Geodetic latitude\", NORTH], \n    AUTHORITY[\"EPSG\",\"4326\"]], \n  PROJECTION[\"Popular Visualisation Pseudo Mercator\", AUTHORITY[\"EPSG\",\"1024\"]], \n  PARAMETER[\"semi_minor\", 6378137.0], \n  PARAMETER[\"latitude_of_origin\", 0.0], \n  PARAMETER[\"central_meridian\", 0.0], \n  PARAMETER[\"scale_factor\", 1.0], \n  PARAMETER[\"false_easting\", 0.0], \n  PARAMETER[\"false_northing\", 0.0], \n  UNIT[\"m\", 1.0], \n  AXIS[\"Easting\", EAST], \n  AXIS[\"Northing\", NORTH], \n  AUTHORITY[\"EPSG\",\"3857\"]]");
            MathTransform to3857 = CRS.findMathTransform(sourceCrs, targetCrs);
            MathTransform to4326 = CRS.findMathTransform(targetCrs, sourceCrs);
            geom = geometry;
            if (!geom.isValid()) {
                geom = GeometryFixer.fix(geom);
            }
            if (bufferInMetre >0D) {
                geom = geom.buffer(bufferdegrees, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT);
            }
            // 数据默认存储的Gcj02,转换为wgs84坐标系进行栅格计算
//            gcj2Wgs(geom);
            // 以3857进行栅格计算
            Geometry regionGeom = JTS.transform(geom, to3857);
            GeomTable table = new GeomTable(regionGeom, 200.0D);
            GeomRow geomRow;
            GeomCell geomCell;
            while ((geomRow = table.nextRow()) != null) {
                while ((geomCell = geomRow.nextCell()) != null) {
                    final Geometry cellGeom = geomCell.getGeom();
                    if (cellGeom != null && !cellGeom.isEmpty()) {
                        Envelope envelope = cellGeom.getEnvelopeInternal();
                        double minX = envelope.getMinX() - gridSize;
                        double maxX = envelope.getMaxX() + gridSize;
                        double minY = envelope.getMinY() - gridSize;
                        double maxY = envelope.getMaxY() + gridSize;
                        for (double x = minX; x < maxX; x += gridSize) {
                            for (double y = minY; y < maxY; y += gridSize) {
                                Coordinate coordinate = new Coordinate(x, y);
                                Coordinate gridCoord = JTS.transform(coordinate, null, to4326);
                                GridOffSetData gridOffset = GridCoordinateGetter.getGridOffset(gridCoord.x, gridCoord.y, gridSize);
                                if (!gridSet.contains(gridOffset)) {
                                    Geometry gridGeom = GridCoordinateGetter.getGridGeometry(gridOffset, gridSize, regionGeom.getFactory());
                                    final Point centroid = gridGeom.getCentroid();
                                    gridGeom = JTS.transform(gridGeom, to3857);
                                    if (gridGeom.intersects(regionGeom)) {
                                        Geometry intersection = gridGeom.intersection(regionGeom);
                                        if (!intersection.isEmpty()) {
                                            gridOffset.setX(centroid.getX());
                                            gridOffset.setY(centroid.getY());
                                            gridSet.add(gridOffset);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return gridSet;
    }
}
