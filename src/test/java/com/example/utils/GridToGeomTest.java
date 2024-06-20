package com.example.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GridToGeomTest {
    public static void main(String[] args) throws Exception {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        String gridFields = "earthID,xOffSet,yOffSet";
        final String[] split = gridFields.split(",", -1);
        String earthId = split[0];
        String gridX = split[1];
        String gridY = split[2];


        final CSVParser parse = csvFormat.parse(new FileReader("D:\\desk\\四川信息职业技术学院东坝校区.csv"));
        final List<String> headerNames = parse.getHeaderNames();
        final ArrayList<String> objects = new ArrayList<>();
        for (String headerName : headerNames) {
            if(!gridFields.contains(headerName)){
                objects.add(headerName);
            }
        }

        final String geom = "geom";
        objects.add(geom);
        CSVFormat csvFormat2 = CSVFormat.DEFAULT.withHeader(objects.toArray(new String[0]));

        final CSVPrinter print = csvFormat2.print(new FileWriter("D:\\desk\\四川信息职业技术学院东坝校区_geom.csv"));
        final Iterator<CSVRecord> iterator = parse.iterator();
        final GeometryFactory geometryFactory = new GeometryFactory();
        while (iterator.hasNext()) {
            final CSVRecord next = iterator.next();
            final Map<String, String> stringStringMap = next.toMap();

            GridOffSetData gridOffset = new GridOffSetData(Integer.parseInt(stringStringMap.get(earthId)), Integer.parseInt(stringStringMap.get(gridX)), Integer.parseInt(stringStringMap.get(gridY)));
            final Geometry gridGeometry = GridCoordinateGetter.getGridGeometry(gridOffset, 20, geometryFactory);
            stringStringMap.put(geom, gridGeometry.toText());
            stringStringMap.remove(earthId);
            stringStringMap.remove(gridX);
            stringStringMap.remove(gridY);
            print.printRecord(stringStringMap.values());
        }
        print.flush();
    }

}
