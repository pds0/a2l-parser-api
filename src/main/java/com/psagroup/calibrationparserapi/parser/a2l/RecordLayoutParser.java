package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.recordlayout.RecordLayout;
import com.psagroup.calibrationparserapi.a2lobject.recordlayout.RecordLayoutBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class RecordLayoutParser extends A2LObjectParser {
    private final Logger logger = LoggerFactory.getLogger(RecordLayoutParser.class);

    private RecordLayoutBuilder rb = new RecordLayoutBuilder();
    private final String a2lObjecttype = "RECORD_LAYOUT";
    private HashMap<String, RecordLayout> record_list = new HashMap<>();

    @Override
    public void parse(BufferedReader reader, String currentLine) throws IOException {
        if (currentLine.replaceAll("\\s+", "").startsWith("/beginRECORD_LAYOUT")) {

            StringBuilder sb = new StringBuilder();
            sb.append(currentLine);
            while (!(currentLine = next(reader)).replaceAll("\\s+", "").startsWith("/endRECORD_LAYOUT")) {
                sb.append("\n ");
                sb.append(currentLine);
            }

            try {
                String recordLayoutContent = sb.toString().trim().replaceAll("/\\*.*\\*/", "");
                RecordLayout recordLayout = buildA2lObject(recordLayoutContent);
                record_list.put(recordLayout.getLabel(), recordLayout);
            } catch (Exception e) {
                logger.error(rb.build().getLabel());
                e.printStackTrace();
            }
        }

    }

    private RecordLayout buildA2lObject(String recordLayoutContent) {
        String[] recordLayoutParts = divideContent(recordLayoutContent);
        rb.label(recordLayoutParts[2]);
        parseDataTypeLR(recordLayoutParts);
        return rb.build();
    }

    /**
     * Parse with keywords what remains in the content after the mandatory fields
     *
     * @param recordLayoutParts Array containing the split String content of the A2lObject
     */
    private void parseDataTypeLR(String[] recordLayoutParts) {
        Integer i = 3;
        while (i < recordLayoutParts.length) {
            switch (recordLayoutParts[i]) {
                case "FNC_VALUES":
                    rb.fncValue(recordLayoutParts[i] + " " + recordLayoutParts[i + 1] + " " + recordLayoutParts[i + 2] + " " + recordLayoutParts[i + 3] + " " + recordLayoutParts[i + 4]);
                    break;
                case "AXIS_PTS_X":
                    rb.axisPtsX(recordLayoutParts[i] + " " + recordLayoutParts[i + 1] + " " + recordLayoutParts[i + 2] + " " + recordLayoutParts[i + 3] + " " + recordLayoutParts[i + 4]);
                    break;
                case "AXIS_PTS_Y":
                    rb.axisPtsY(recordLayoutParts[i] + " " + recordLayoutParts[i + 1] + " " + recordLayoutParts[i + 2] + " " + recordLayoutParts[i + 3] + " " + recordLayoutParts[i + 4]);
                    break;
                case "NO_AXIS_PTS_X":
                    rb.noAxisPtsX(recordLayoutParts[i] + " " + recordLayoutParts[i + 1] + " " + recordLayoutParts[i + 2]);
                    break;
                case "NO_AXIS_PTS_Y":
                    rb.noAxisPtsY(recordLayoutParts[i] + " " + recordLayoutParts[i + 1] + " " + recordLayoutParts[i + 2]);
                    break;
                default:
                    break;
            }
            i++;
        }
    }

    /**
     * @return the record_list
     */
    HashMap<String, RecordLayout> getRecordList() {
        return record_list;
    }

}
