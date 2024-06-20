package com.example.utils;



import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.io.*;
import java.util.*;


/**
 * Date: 2020/11/26
 * Description: <描述>
 *
 * @Author: Oyoyoyoyoyoyo
 */
public class GeomTable {
    private final Geometry geom;
    private final double cellSize;
    private Stack<Geometry> rowGeomStack = new Stack();
    private Stack<Geometry> tableGeomStack = new Stack();

    public static void main(String[] args) throws Exception {
        WKBReader wkbReader = new WKBReader();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\10100674\\Desktop\\安徽\\34100highway_section.csv"), "UTF-8"));
        Throwable var3 = null;

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:\\Users\\10100674\\Desktop\\安徽\\aaaaa.csv", false), "UTF-8"));
            Throwable var5 = null;

            try {
                String s;
                try {
                    while ((s = bufferedReader.readLine()) != null) {
                        Geometry geometry = wkbReader.read(WKBReader.hexToBytes(s));
                        System.out.println(geometry.toText());
                        out.write("\"" + geometry.toText() + "\"");
                        out.write("\n");
                        out.flush();
                    }
                } catch (Throwable var29) {
                    var5 = var29;
                    throw var29;
                }
            } finally {
                if (out != null) {
                    if (var5 != null) {
                        try {
                            out.close();
                        } catch (Throwable var28) {
                            var5.addSuppressed(var28);
                        }
                        Object var28 = null;
                    } else {
                        out.close();
                    }
                }

            }
        } catch (Throwable var31) {
            var3 = var31;
            throw var31;
        } finally {
            if (bufferedReader != null) {
                if (var3 != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable var27) {
                        var3.addSuppressed(var27);
                    }
                } else {
                    bufferedReader.close();
                }
            }

        }

        //Geometry gridGeometry = main.GridGetter.GridCoordinateGetter.getGridGeometry(new main.GridGetter.GridOffSetData(4841, 14785, 14204), 5.0D);
        //GridOffSetData gridOffset = GridCoordinateGetter.getGridOffset(gridGeometry.getCentroid().getX(), gridGeometry.getCentroid().getY(), 50.0D);
        WKTReader wktReader = new WKTReader();
        Geometry geometry = wktReader.read("POLYGON ((111.37802124023438 26.851029008675006, 111.33956909179688 26.846127990018164, 111.70486450195312 26.309419586402797, 111.72958374023438 26.32172929955373, 111.37802124023438 26.851029008675006))");
        GeomTable table = new GeomTable(geometry, 0.002D);
        int i = 0;

        while (true) {
            GeomRow geomRow = table.nextRow();
            if (geomRow == null) {
                i = 0;
                GeomTable geomTabletable = new GeomTable(geometry, 0.002D);
                GeomCell geomCell;
                while ((geomRow = geomTabletable.nextRow()) != null) {
                    while ((geomCell = geomRow.nextCell()) != null) {
                        ++i;
                        System.out.println(i + "test");
                        //System.out.println(geomCell.getGeom().toText());
                    }
                }

                return;
            }

            while (true) {
                GeomCell geomCell = geomRow.nextCell();
                if (geomCell == null) {
                    break;
                }

                Geometry geom = geomCell.getGeom();
                if (geom != null && !geom.isEmpty()) {
                    ++i;
                    System.out.println(i);
                    System.out.println(geom.toText());
                }
            }
        }
    }

    public GeomTable(Geometry geom, double cellSize) {
        this.geom = geom;
        this.tableGeomStack.add(geom);
        this.cellSize = cellSize;
    }

    public GeomRow nextRow() {
        this.doNextRow();
        if (this.rowGeomStack.isEmpty()) {
            return null;
        } else {
            return this.rowGeomStack.peek() == null ? null : new GeomRow((Geometry) this.rowGeomStack.pop(), this.cellSize);
        }
    }

    private void doNextRow() {
        while (this.rowGeomStack.isEmpty()) {
            if (this.tableGeomStack.isEmpty()) {
                return;
            }

            this.nextRowFromTable();
        }

    }

    private void nextRowFromTable() {
        Geometry table = (Geometry) this.tableGeomStack.pop();
        List<Geometry> rows = this.split2RowsInternal(table);
        Iterator var3 = rows.iterator();

        while (var3.hasNext()) {
            Geometry geom = (Geometry) var3.next();
            if (geom != null && !geom.isEmpty()) {
                if (geom.equals(table)) {
                    this.rowGeomStack.add(geom);
                } else if (this.isRowGeom(geom)) {
                    this.rowGeomStack.add(geom);
                } else {
                    this.tableGeomStack.add(geom);
                }
            }
        }

    }

    public Geometry getGeom() {
        return this.geom;
    }

    private boolean isRowGeom(Geometry geom) {
        Envelope envelopeInternal = geom.getEnvelopeInternal();
        Coordinate centre = envelopeInternal.centre();
        Coordinate top = new Coordinate(centre.x, envelopeInternal.getMinY());
        Coordinate bottom = new Coordinate(centre.x, envelopeInternal.getMaxY());
        double bufferFactor = 1.1D;
        return top.distance(bottom) <= this.cellSize;
    }

    private List<Geometry> split2RowsInternal(Geometry geom) {
        MultiLineString splitLine = this.getFirstSplitLine(geom);
        GeometryFactory geometryFactory = geom.getFactory();
        List<Geometry> polygonsToKeep = new ArrayList();
        if (!geom.intersects(splitLine)) {
            polygonsToKeep.add(geom);
            return polygonsToKeep;
        } else {
            Geometry union = geom.getBoundary().union(splitLine);
            Polygonizer polygonizer = new Polygonizer();
            polygonizer.add(union);
            Collection<Geometry> polygons = polygonizer.getPolygons();
            MultiPolygon multiPolygon = geometryFactory.createMultiPolygon((Polygon[]) polygons.toArray(new Polygon[0]));
            int numGeometries = multiPolygon.getNumGeometries();

            for (int i = 0; i < numGeometries; ++i) {
                Geometry geometryN = multiPolygon.getGeometryN(i);
                if (geom.contains(geometryN.getInteriorPoint())) {
                    polygonsToKeep.add(geometryN);
                }
            }

            return polygonsToKeep;
        }
    }

    private MultiLineString getFirstSplitLine(Geometry geom) {
        Envelope envelopeInternal = geom.getEnvelopeInternal();
        Coordinate centre = envelopeInternal.centre();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate left = new Coordinate(envelopeInternal.getMinX(), envelopeInternal.getMinY() + this.cellSize);
        Coordinate right = new Coordinate(envelopeInternal.getMaxX(), envelopeInternal.getMinY() + this.cellSize);
        Coordinate[] coords = new Coordinate[]{left, right};
        LineString line = geometryFactory.createLineString(coords);
        LineString[] lines = new LineString[]{line};
        return geometryFactory.createMultiLineString(lines);
    }
}
