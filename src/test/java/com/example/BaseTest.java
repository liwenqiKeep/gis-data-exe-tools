package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseTest {

    public List<String> getValues(List<String> headerNames, Map<String, String> record) {
        List<String> values = new ArrayList<>();
        for (String headerName : headerNames) {
            values.add(record.get(headerName));
        }
        return values;
    }

}
