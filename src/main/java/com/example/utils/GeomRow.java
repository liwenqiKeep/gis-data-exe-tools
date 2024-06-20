package com.example.utils;


import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;

import java.util.*;

/**
 * Date:2020/11/26
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */
public class GeomRow {
    private final Geometry geom;
    private final double cellSize;
    private Stack<Geometry> cellGeomStack = new Stack();
    private Stack<Geometry> rowGeomStack = new Stack();

    public GeomRow(Geometry geom, double cellSize) {
        this.geom = geom;
        this.rowGeomStack.add(geom);
        this.cellSize = cellSize;
    }

    public GeomCell nextCell() {
        this.doNextCell();
        if (this.cellGeomStack.isEmpty()) {
            return null;
        } else {
            return this.cellGeomStack.peek() == null ? null : new GeomCell((Geometry) this.cellGeomStack.pop());
        }
    }

    private void doNextCell() {
        while (this.cellGeomStack.isEmpty()) {
            if (this.rowGeomStack.isEmpty()) {
                return;
            }

            this.nextCellFromRow();
        }

    }

    private void nextCellFromRow() {
        Geometry pop = (Geometry) this.rowGeomStack.pop();
        List<Geometry> geomCells = this.split2CellsInternal(pop);
        Iterator var3 = geomCells.iterator();

        while (var3.hasNext()) {
            Geometry geom = (Geometry) var3.next();
            if (geom != null && !geom.isEmpty()) {
                if (geom.equals(pop)) {
                    this.cellGeomStack.add(geom);
                } else if (this.isCell(geom)) {
                    this.cellGeomStack.add(geom);
                } else {
                    this.rowGeomStack.add(geom);
                }
            }
        }

    }

    public Geometry getGeom() {
        return this.geom;
    }

    private boolean isCell(Geometry geom) {
        if (geom != null && !geom.isEmpty()) {
            Envelope envelopeInternal = geom.getEnvelopeInternal();
            Coordinate centre = envelopeInternal.centre();
            Coordinate left = new Coordinate(envelopeInternal.getMinX(), centre.y);
            Coordinate right = new Coordinate(envelopeInternal.getMaxX(), centre.y);
            double bufferFactor = 1.1D;
            if (left.distance(right) <= this.cellSize) {
                return true;
            }
        }

        return false;
    }

    private List<Geometry> split2CellsInternal(Geometry geom) {
        MultiLineString splitLine = this.getFirstSplitLine(geom);
        GeometryFactory geometryFactory = new GeometryFactory();
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
        Coordinate top = new Coordinate(envelopeInternal.getMinX() + this.cellSize, envelopeInternal.getMinY());
        Coordinate bottom = new Coordinate(envelopeInternal.getMinX() + this.cellSize, envelopeInternal.getMaxY());
        Coordinate[] coords = new Coordinate[]{top, bottom};
        LineString line = geometryFactory.createLineString(coords);
        LineString[] lines = new LineString[]{line};
        return geometryFactory.createMultiLineString(lines);
    }
}
