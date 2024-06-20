package com.example.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.*;
import java.util.*;

public class PolygonText {
    public static void main(String[] args) throws IOException {
        File outFile = new File("D:\\desk\\scene_gjgx.csv");
        File gridFile = new File("D:\\desk\\scene_gjgx_grid_20.csv");
        String geomFieldName = "geom";
        int type = 1;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
//        String splitFileCols = "lon,lat,section_id";
        String gridFileCols = "regionid,xoffset,yoffset";
        final WKBWriter wkbWriter = new WKBWriter();
        final WKTReader wktReader = new WKTReader();

        try (
                final CSVParser csvParser = csvFormat.parse(new FileReader(outFile));
                final FileOutputStream out2 = new FileOutputStream(gridFile, false);
                final BufferedWriter fileWriter2 = new BufferedWriter(new OutputStreamWriter(out2));

        ) {
            final List<String> headerNames = csvParser.getHeaderNames();
            final ArrayList<String> strings2 = new ArrayList<>(headerNames);
            strings2.addAll(List.of(gridFileCols.split(",")));
            strings2.remove(geomFieldName);
            final CSVFormat outFormat2 = CSVFormat.DEFAULT.withHeader(strings2.toArray(new String[0]));
            final CSVPrinter printer2 = new CSVPrinter(fileWriter2, outFormat2);
            final long recordNumber = csvParser.getRecordNumber();
            System.out.println(recordNumber);
            final Iterator<CSVRecord> iterator = csvParser.iterator();
            byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            out2.write(bytes);

            while (iterator.hasNext()) {
                final CSVRecord next = iterator.next();
                final String s = next.get(geomFieldName);
                try {
                    boolean iswkt = s.contains("(");
                    Geometry read;
                    if (iswkt) {
                        read = wktReader.read(s);
                    } else {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    }
                    final Map<String, String> stringStringMap = next.toMap();
                    if(!(read instanceof MultiPolygon || read instanceof Polygon)){
                        continue;
                    }

                    final HashSet<GridOffSetData> gridOffSetData = GridUtils.gridRecords(read, 20d, 0d);
                    for (GridOffSetData gridOffSetData1 : gridOffSetData) {
                        final HashMap<String, String> record2 = new HashMap<>(stringStringMap);
                        record2.put("regionid", String.valueOf(gridOffSetData1.earthID));
                        record2.put("xoffset", String.valueOf(gridOffSetData1.xOffSet));
                        record2.put("yoffset", String.valueOf(gridOffSetData1.yOffSet));
                        printer2.printRecord(getValues(strings2, record2));
                    }
                } catch (Exception e) {
                    System.out.println("执行出错：" + next.toString());
                }

            }
        }
    }
    private static List<String> getValues(List<String> headerNames, HashMap<String, String> record) {
        List<String> values = new ArrayList<>();
        for (String headerName : headerNames) {
            values.add(record.get(headerName));
        }
        return values;
    }
}
