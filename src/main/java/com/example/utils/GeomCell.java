package com.example.utils;


import org.locationtech.jts.geom.Geometry;

/**
 * Date:2020/11/26
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */
public class GeomCell {
    private final Geometry geom;

    public GeomCell(Geometry geom) {
        this.geom = geom;
    }

    public Geometry getGeom() {
        return this.geom;
    }
}
