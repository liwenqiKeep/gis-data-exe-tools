package com.example.utils;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.math.BigDecimal;

/**
 * @author Liwq
 */
public class GeomCommonUtils {
    /**
     * 距离(m)到度的转换
     * 转换算法：degree=length/(2*Math.PI*6371004)*360;
     *
     * @param length 缓冲区距离 单位m
     * @return
     */
    public static double mToDegrees(double length) {
        //100米=0.0008983153 Degrees
        //degree = meter / (2 * Math.PI * 6371004) * 360
        return length / (2 * Math.PI * 6371004) * 360;
    }

    /**
     * geom 缓冲区
     * 使用 AUTO:42001 进行转化，得到的缓存区精度更高
     *
     * @param geometry      geom
     * @param bufferInMetre 缓冲半径，米为单位
     * @return geom
     * @throws Exception
     */
    public static Geometry autoBuffer(Geometry geometry, double bufferInMetre) throws Exception {
        final Point coordinate = geometry.getCentroid();
        String crsAuto = "AUTO:42001," + coordinate.getCoordinate().x + "," + coordinate.getCoordinate().y;
        CoordinateReferenceSystem source = DefaultGeographicCRS.WGS84;
        CoordinateReferenceSystem target = CRS.decode(crsAuto);

        final Geometry transform = JTS.transform(geometry, CRS.findMathTransform(source, target));
        return JTS.transform(transform.buffer(bufferInMetre, BufferParameters.DEFAULT_QUADRANT_SEGMENTS, BufferParameters.CAP_FLAT), CRS.findMathTransform(target, source));
    }

    public static void main(String[] args) throws ParseException {

        System.out.println(
               new BigDecimal(mToDegrees(100)) .toString()
        );
    }
}
