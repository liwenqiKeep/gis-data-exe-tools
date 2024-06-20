package com.example.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Date:2020/11/25
 * Decription:<描述>
 *
 * @Author:oyoyoyoyoyoyo
 */


public class EarthDivisionDataLoader {
    public EarthDivisionDataLoader() {
    }

    public HashMap<Integer, EarthDivisionData> loadEarthDivisionData(String filePath) {
        try {
            HashMap<Integer, EarthDivisionData> divDataMap = new HashMap();
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(filePath);

            BufferedReader readData = new BufferedReader(new InputStreamReader(resourceAsStream, "UTF-8"));
            String lineData = readData.readLine();

            while ((lineData = readData.readLine()) != null) {
                String[] oneLineData = lineData.split(",");
                EarthDivisionData divData = new EarthDivisionData();

                try {
                    divData.earthID = Integer.parseInt(oneLineData[0]);
                    divData.centerLon = Double.parseDouble(oneLineData[1]);
                    divData.centerLat = Double.parseDouble(oneLineData[2]);
                    divData.centerX = Double.parseDouble(oneLineData[3]);
                    divData.centerY = Double.parseDouble(oneLineData[4]);
                    divData.leftLost = Double.parseDouble(oneLineData[5]);
                    divData.realLeftLon = Double.parseDouble(oneLineData[6]);
                    divData.realRightLon = Double.parseDouble(oneLineData[7]);
                    divData.realTopLat = Double.parseDouble(oneLineData[8]);
                    divData.realBottomLat = Double.parseDouble(oneLineData[9]);
                    divData.anglePer100mAlongLon = Double.parseDouble(oneLineData[10]);
                    divData.anglePer100mAlongLat = Double.parseDouble(oneLineData[11]);
                    divDataMap.put(divData.earthID, divData);
                } catch (NumberFormatException var9) {
                    return null;
                }
            }

            readData.close();
            return divDataMap;
        } catch (Exception var10) {
            return null;
        }
    }
}
