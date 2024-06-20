package com.example.utils;

import com.example.BaseTest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WkbToWktText  extends BaseTest {


    @Test
    public void test() {
        File file = new File("D:\\desk\\dgis_data\\共建共享\\共建共享数据同步\\05-27\\cfg_scene_entity_gjgx20240527.csv");
        File outFile = new File("D:\\desk\\dgis_data\\共建共享\\共建共享数据同步\\05-27\\cfg_scene_entity_gjgx20240527_wkt.csv");
        String geomFieldName = "geom";
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        try(
                final CSVParser parse = csvFormat.parse(new FileReader(file));

        ){
            CSVFormat outCsvFormat = CSVFormat.DEFAULT.withHeader(parse.getHeaderNames().toArray(new String[0]));
            final CSVPrinter print = outCsvFormat.print(new FileWriter(outFile));
            final WKBReader wkbReader = new WKBReader();
            final Iterator<CSVRecord> iterator = parse.iterator();
            while (iterator.hasNext()){
                CSVRecord record = iterator.next();
                String geom = record.get(geomFieldName);
                final Geometry read = wkbReader.read(WKBReader.hexToBytes(geom));
                final Map<String, String> stringStringMap = record.toMap();
                stringStringMap.replace(geomFieldName, read.toText());
                print.printRecord(stringStringMap.values());
            }
        }catch (Exception e){

        }
    }

    @Test
    public void test2() {
        File file = new File("D:\\desk\\hunan-hw_vector_allline_single_split100m_with_adminarea.csv");
        File outFile = new File("D:\\desk\\hunan-hw_vector_allline_single_split100m_with_adminarea_wkb.csv");
        String geomFieldName = "geom";
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        try(
                final CSVParser parse = csvFormat.parse(new FileReader(file));

        ){
            CSVFormat outCsvFormat = CSVFormat.DEFAULT.withHeader(parse.getHeaderNames().toArray(new String[0]));
            final CSVPrinter print = outCsvFormat.print(new FileWriter(outFile));
            final WKBWriter wkbWriter = new WKBWriter();
            final WKTReader wktReader = new WKTReader();
            final Iterator<CSVRecord> iterator = parse.iterator();
            while (iterator.hasNext()){
                CSVRecord record = iterator.next();
                String geom = record.get(geomFieldName);
                final Geometry read = wktReader.read(geom);
                final Map<String, String> stringStringMap = record.toMap();
                stringStringMap.replace(geomFieldName, WKBWriter.toHex(wkbWriter.write(read)));
                print.printRecord(stringStringMap.values());
            }
        }catch (Exception e){

        }
    }

    public static void main(String[] args) throws ParseException {
        final WKBReader wkbReader = new WKBReader();
        final Geometry read = wkbReader.read(WKBReader.hexToBytes(""));
        final GeometryFactory geometryFactory = new GeometryFactory();
        double lon = 112.0;
        double lat = 22.0;
        Coordinate coordinate = new CoordinateXY(lon,lat);

        final Point point = geometryFactory.createPoint(coordinate);
        System.out.println(read.intersects(point));
    }

    @Test
    public void test3() {
        File file = new File("D:\\desk\\hunan-hw_vector_allline_single_split100m_grid20m.csv");
        File outFile = new File("D:\\desk\\cfg_expressway_section100m_grid20m_rel.csv");
//        String geomFieldName = "geom";
        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader();
        String[] outHeader = new String[]{"\uFEFFline_id", "name", "sectionId","regionid","xoffset","yoffset"};
        try(
                final CSVParser parse = csvFormat.parse(new FileReader(file));

        ){
            CSVFormat outCsvFormat = CSVFormat.DEFAULT.withHeader(outHeader);
            final CSVPrinter print = outCsvFormat.print(new FileWriter(outFile));
            final Iterator<CSVRecord> iterator = parse.iterator();
//            final List<CSVRecord> records = parse.getRecords();
//            final List<CSVRecord> collect = records.stream().sorted((o1, o2) -> {
//                int l1 = Integer.parseInt(o1.get("line_id"));
//                int l2 = Integer.parseInt(o2.get("line_id"));
//                int s1 = Integer.parseInt(o1.get("sectionId"));
//                int s2 = Integer.parseInt(o2.get("sectionId"));
//                if (l1 == l2) {
//                    return s1 - s2;
//                } else {
//                    return l1 - l2;
//                }
//
//            }).collect(Collectors.toList());
            for (; iterator.hasNext(); ) {
                CSVRecord record = iterator.next();
                print.printRecord(getValues(Arrays.asList(outHeader), record.toMap()));
            }
            print.flush();
            print.close();
        }catch (Exception e){

        }
    }
}
