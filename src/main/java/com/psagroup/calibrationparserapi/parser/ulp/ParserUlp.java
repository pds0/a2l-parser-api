package com.psagroup.calibrationparserapi.parser.ulp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ParserUlp {
    private Map<String, String> records;
    private BufferedReader reader;


    public ParserUlp(InputStream stream) {
        this.reader = new BufferedReader(new InputStreamReader(stream));
        this.records = new HashMap<>();
    }

    public void parse() throws IOException, SRecordParseException {
        String recordStr;
        while ((recordStr = reader.readLine()) != null) {
            SRecord sRecord = new SRecord(recordStr);
            String address = sRecord.getAddress();
            if (sRecord.getType() == 3) {
                String data = sRecord.getData();
                records.put(address.toLowerCase(), formatData(data));
            }
        }
    }


    private String formatData(String data) {
        String[] dataTab = splitStringEvery(data, 2);
        return String.join(" ", dataTab);
    }

    private String[] splitStringEvery(String s, int interval) {
        int arrayLength = (int) Math.ceil(((s.length() / (double) interval)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = "0x" + s.substring(j, j + interval);
            j += interval;
        } //Add the last bit
        result[lastIndex] = "0x" + s.substring(j);

        return result;
    }

    public Map<String, String> getRecords() {
        return records;
    }
}
