package com.example.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

class SplitLineTest {

    @Test
    public void csvLineSplit() throws Exception {
        File file = new File("D:\\desk\\dgis_data\\xizang\\road_all.csv");
        File outFile = new File("D:\\desk\\dgis_data\\xizang\\road_all_split20m.csv");
//        File gridFile = new File("D:\\desk\\dgis_data\\xizang\\road_all_split20m_grid20m.csv");
        String geomFieldName = "WKT";
        int type = 1;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
        String splitFileCols = "lon,lat,section_id";
//        String gridFileCols = "regionid,xoffset,yoffset,section_id";
        final WKBWriter wkbWriter = new WKBWriter();
        final WKTReader wktReader = new WKTReader();

        try (final CSVParser csvParser = csvFormat.parse(new FileReader(file));

             final FileOutputStream out = new FileOutputStream(outFile, false);
             final BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(out));
//             final FileOutputStream out2 = new FileOutputStream(gridFile, false);
//             final BufferedWriter fileWriter2 = new BufferedWriter(new OutputStreamWriter(out2));

        ) {
            final List<String> headerNames = csvParser.getHeaderNames();
            final ArrayList<String> strings = new ArrayList<>(headerNames);
            strings.addAll(List.of(splitFileCols.split(",")));
//            headerNames.addAll(new ArrayList<>());
            final CSVFormat outFormat = CSVFormat.DEFAULT.withHeader(strings.toArray(new String[0]));
            final CSVPrinter printer = new CSVPrinter(fileWriter, outFormat);

//            final ArrayList<String> strings2 = new ArrayList<>(headerNames);
//            strings2.addAll(List.of(gridFileCols.split(",")));
//            strings2.remove(geomFieldName);
//            final CSVFormat outFormat2 = CSVFormat.DEFAULT.withHeader(strings2.toArray(new String[0]));
//            final CSVPrinter printer2 = new CSVPrinter(fileWriter2, outFormat2);

            final Iterator<CSVRecord> iterator = csvParser.iterator();
            byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            out.write(bytes);

            while (iterator.hasNext()) {
                final CSVRecord next = iterator.next();
                final String s = next.get(geomFieldName);
                try {
                    boolean iswkt = false;
                    if (s.contains("(")) {
                        iswkt = true;
                    }
                    Geometry read;
                    if (iswkt) {
                        read = wktReader.read(s);
                    } else {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    }
                    double segmentLength = this.getSplitLength(type, next);
                    List<LineString> lineStrings = SplitLine.split2Segments(read, segmentLength);
                    //去除小于2米的段,经验值，2米段做buffer会有问题
                    lineStrings.removeIf(lineString -> lineString.getLength() < GeomCommonUtils.mToDegrees(2));
                    final Map<String, String> stringStringMap = next.toMap();
                    for (int i = 0; i < lineStrings.size(); i++) {
                        int sectionid = i + 1;
                        final LineString lineString = lineStrings.get(i);
                        final Point centroid = lineString.getCentroid();
                        final HashMap<String, String> record = new HashMap<>(stringStringMap);
                        record.replace(geomFieldName, WKBWriter.toHex(wkbWriter.write(lineString)));
                        record.put("lon", BigDecimal.valueOf(centroid.getX()).toString());
                        record.put("lat", BigDecimal.valueOf(centroid.getY()).toString());
                        record.put("section_id", String.valueOf(sectionid));


                        printer.printRecord(getValues(strings, record));
//
//                        final HashSet<GridOffSetData> gridOffSetData = GridUtils.gridRecords(lineString, 20d, 10d);
//                        for (GridOffSetData gridOffSetData1 : gridOffSetData) {
//                            final HashMap<String, String> record2 = new HashMap<>(stringStringMap);
//                            record2.put("section_id", String.valueOf(sectionid));
//                            record2.put("regionid", String.valueOf(gridOffSetData1.earthID));
//                            record2.put("xoffset", String.valueOf(gridOffSetData1.xOffSet));
//                            record2.put("yoffset", String.valueOf(gridOffSetData1.yOffSet));
//                            printer2.printRecord(getValues(strings2, record2));
//                        }
                    }

                } catch (Exception e) {
                    System.out.println("执行出错：" + next.toString());
                }

            }
        }

    }

    @Test
    public void lineBufferAndGrid() throws Exception {
//        File file = new File("D:\\desk\\dgis_data\\xizang\\road_all.csv");
        File outFile = new File("D:\\desk\\hunan-hw_vector_allline_single_split100m.csv");
        File gridFile = new File("D:\\desk\\hunan-hw_vector_allline_single_split100m_grid20m.csv");
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

                    final HashSet<GridOffSetData> gridOffSetData = GridUtils.gridRecords(read, 20d, 10d);
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

    private List<String> getValues(List<String> headerNames, HashMap<String, String> record) {
        List<String> values = new ArrayList<>();
        for (String headerName : headerNames) {
            values.add(record.get(headerName));
        }
        return values;
    }

    private double getSplitLength(int type, CSVRecord next) {
        switch (type) {
            case 2:
                if ("国道".equals(next.get("type"))) {
                    return 100D;
                } else {
                    return 20D;
                }
            default:
                return 20;
        }
    }

    @Test
    public void csvLineSplit2() throws Exception {
        File file = new File("D:\\desk\\china_highway_allline.csv");
        File outFile = new File("D:\\desk\\china_highway_allline_split100m.csv");
        String geomFieldName = "WKT";
        int type = 1;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
        String splitFileCols = "lon,lat,section_id";
        final WKBWriter wkbWriter = new WKBWriter();
        final WKTReader wktReader = new WKTReader();

        try (final CSVParser csvParser = csvFormat.parse(new FileReader(file));

             final FileOutputStream out = new FileOutputStream(outFile, false);
             final BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(out));

        ) {
            final List<String> headerNames = csvParser.getHeaderNames();
            final ArrayList<String> strings = new ArrayList<>(headerNames);
            strings.addAll(List.of(splitFileCols.split(",")));

            final CSVFormat outFormat = CSVFormat.DEFAULT.withHeader(strings.toArray(new String[0]));
            final CSVPrinter printer = new CSVPrinter(fileWriter, outFormat);


            final Iterator<CSVRecord> iterator = csvParser.iterator();
            byte[] bytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            out.write(bytes);

            while (iterator.hasNext()) {
                final CSVRecord next = iterator.next();
                final String s = next.get(geomFieldName);
                try {
                    boolean iswkt = false;
                    if (s.contains("(")) {
                        iswkt = true;
                    }
                    Geometry read;
                    if (iswkt) {
                        read = wktReader.read(s);
                    } else {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    }
                    double segmentLength = 100D;
                    List<LineString> lineStrings = SplitLine.split2Segments(read, segmentLength);
                    //去除小于2米的段,经验值，2米段做buffer会有问题
                    lineStrings.removeIf(lineString -> lineString.getLength() < GeomCommonUtils.mToDegrees(2));
                    final Map<String, String> stringStringMap = next.toMap();
                    for (int i = 0; i < lineStrings.size(); i++) {
                        int sectionid = i + 1;
                        final LineString lineString = lineStrings.get(i);
                        final Point centroid = lineString.getCentroid();
                        final HashMap<String, String> record = new HashMap<>(stringStringMap);
                        record.replace(geomFieldName, WKBWriter.toHex(wkbWriter.write(lineString)));
                        record.put("lon", BigDecimal.valueOf(centroid.getX()).toString());
                        record.put("lat", BigDecimal.valueOf(centroid.getY()).toString());
                        record.put("section_id", String.valueOf(sectionid));


                        printer.printRecord(getValues(strings, record));
                    }

                } catch (Exception e) {
                    System.out.println("执行出错：" + next.toString());
                }

            }
        }

    }
    @Test
    public void lineBufferAndGrid2() throws Exception {
        File outFile = new File("D:\\desk\\china_highway_allline_split100m.csv");
        File gridFile = new File("D:\\desk\\china_highway_allline_split100m_buffer150m_grid20m.csv");
        String geomFieldName = "\uFEFFWKT";
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
        String gridFileCols = "regionid,xoffset,yoffset,grid_lon,grid_lat";
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

                    final HashSet<GridOffSetData> gridOffSetData = GridUtils.gridRecords(read, 20d, 150d);
                    for (GridOffSetData gridOffSetData1 : gridOffSetData) {
                        final HashMap<String, String> record2 = new HashMap<>(stringStringMap);
                        record2.put("regionid", String.valueOf(gridOffSetData1.earthID));
                        record2.put("xoffset", String.valueOf(gridOffSetData1.xOffSet));
                        record2.put("yoffset", String.valueOf(gridOffSetData1.yOffSet));
                        record2.put("grid_lon", BigDecimal.valueOf(gridOffSetData1.getX()).toString());
                        record2.put("grid_lat", BigDecimal.valueOf(gridOffSetData1.getY()).toString());
                        printer2.printRecord(getValues(strings2, record2));
                    }
                } catch (Exception e) {
                    System.out.println("执行出错：" + next.toString());
                }

            }
        }
    }
}