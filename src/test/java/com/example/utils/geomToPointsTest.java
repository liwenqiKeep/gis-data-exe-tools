package com.example.utils;

import com.example.BaseTest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class geomToPointsTest extends BaseTest {

    @Test
    public void geomToPoints() throws Exception {

        File file = new File("D:\\desk\\hunan-hw_vector_allline.csv");
        File outFile = new File("D:\\desk\\hunan-hw_vector_allline_points.csv");
        String geomFieldName = "geoc_gcj02towgs84";
//        List<String> outFields = List.of("line_id","lon","lat");
        String[] outFields = {"line_id", "lon", "lat"};
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        CSVFormat outCsvFormat = CSVFormat.DEFAULT.withHeader(outFields);
        final WKBReader wkbReader = new WKBReader();
        final WKTReader wktReader = new WKTReader();
        try (final CSVParser csvParser = csvFormat.parse(new FileReader(file));
             final CSVPrinter print = outCsvFormat.print(new FileWriter(outFile))
        ) {
            final Iterator<CSVRecord> iterator = csvParser.iterator();
            while (iterator.hasNext()) {
                final CSVRecord next = iterator.next();
                final String s = next.get(geomFieldName);
                try {
                    boolean iswkt = false;
                    if (s.contains("(") || s.contains("EMPTY")) {
                        iswkt = true;
                    }
                    Geometry read;
                    if (iswkt) {
                        read = wktReader.read(s);
                    } else {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    }

                    final Coordinate[] coordinates = read.getCoordinates();
                    final String line_id = next.get("\uFEFFline_id");
                    for (int i = 0; i < coordinates.length; i++) {
                        final ArrayList<String> objects = new ArrayList<>();
                        objects.add(line_id);
                        final Coordinate coordinate = coordinates[i];
                        objects.add(BigDecimal.valueOf(coordinate.x).toString());
                        objects.add(BigDecimal.valueOf(coordinate.y).toString());
                        print.printRecord(objects);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("执行出错：" + next.toString());
                }
            }
        }
    }

    @Test
    public void lineBufferAndGrid() throws Exception {
//        File file = new File("D:\\desk\\dgis_data\\xizang\\road_all.csv");
        File outFile = new File("D:\\desk\\hunan_wangge_vpm.csv");
        File gridFile = new File("D:\\desk\\hunan_wangge_vmap.csv");
        String geomFieldName = "WKT";
        int type = 1;
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
//        String splitFileCols = "lon,lat,section_id";
        String gridFileCols = "regionid,xoffset,yoffset";
        final WKBWriter wkbWriter = new WKBWriter();
        final WKTReader wktReader = new WKTReader();

        try (
                final CSVParser csvParser = csvFormat.parse(new FileReader(outFile));

//             final FileOutputStream out = new FileOutputStream(outFile, false);
//             final BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(out));
                final FileOutputStream out2 = new FileOutputStream(gridFile, false);
                final BufferedWriter fileWriter2 = new BufferedWriter(new OutputStreamWriter(out2));

        ) {
            final List<String> headerNames = csvParser.getHeaderNames();


            final ArrayList<String> strings2 = new ArrayList<>(headerNames);
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
                    boolean iswkt = false;
                    if (s.contains("(") || s.contains("EMPTY")) {
                        iswkt = true;
                    }
                    Geometry read;
                    if (iswkt) {
                        read = wktReader.read(s);
                    } else {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    }
                    final Map<String, String> stringStringMap = next.toMap();
                    final Coordinate[] coordinates = read.getCoordinates();
                    StringBuilder stringBuffer = new StringBuilder();
                    for (Coordinate coordinate : coordinates) {
                        stringBuffer.append(coordinate.x).append(" ").append(coordinate.y).append(",");
                    }


                    stringStringMap.replace(geomFieldName, stringBuffer.length() > 0 ? stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString() : "");
                    printer2.printRecord(getValues(strings2, stringStringMap));

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("执行出错：" + next.toString());
                }

            }
        }
    }

    public void specialLineSpit() throws Exception {
        File file = new File("D:\\desk\\hunan_gaosu.csv");
        File outFile = new File("D:\\desk\\hunan_gaosu_split100m.csv");
        String geomFieldName = "st_union";
        String sortFieldName = "section_id";
        String groupFieldName = "line_id";

        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        final WKBReader wkbReader = new WKBReader();
        String splitFileCols = "lon,lat,section_id";
        final WKBWriter wkbWriter = new WKBWriter();
        final WKTReader wktReader = new WKTReader();
        final CSVParser parse = csvFormat.parse(new FileReader(file));
        final List<CSVRecord> records = parse.getRecords();
        final Map<String, List<CSVRecord>> groupByList = records.stream().collect(Collectors.groupingBy(record -> record.get(groupFieldName)));
        groupByList.forEach((k, v) -> {
            final List<CSVRecord> sortedList = v.stream().sorted(Comparator.comparing(record -> record.get(sortFieldName))).collect(Collectors.toList());
            Geometry lastGeom = null;
            for (CSVRecord record : sortedList) {
                int section = 1;
                final String s = record.get(sortFieldName);
                boolean iswkt = false;
                if (s.contains("(") || s.contains("EMPTY")) {
                    iswkt = true;
                }
                Geometry read = null;
                if (iswkt) {
                    try {
                        read = wktReader.read(s);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        read = wkbReader.read(WKBReader.hexToBytes(s));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (lastGeom == null) {

                } else {
                    if (read instanceof LineString) {

                    }
                }

            }
        });

    }
}
