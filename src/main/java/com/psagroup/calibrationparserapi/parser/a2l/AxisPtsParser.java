package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.axis.AxisPts;
import com.psagroup.calibrationparserapi.a2lobject.axis.AxisPtsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class AxisPtsParser extends A2LObjectParser {
    private final Logger logger = LoggerFactory.getLogger(AxisPtsParser.class);

    AxisPtsBuilder ab = new AxisPtsBuilder();
    static final String a2lObjectType = "AXIS_PTS";
    public Map<String, AxisPts> axisPtsList = new HashMap<>();

    @Override
    public void parse(BufferedReader reader, String currentLine) throws IOException {
        if (currentLine.replaceAll("\\s+", "").startsWith("/beginAXIS_PTS")) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(currentLine);
                while (!(currentLine = next(reader)).replaceAll("\\s+", "").startsWith("/endAXIS_PTS")) {
                    sb.append("\n ");
                    sb.append(currentLine);
                }
                String axisPtsContent = sb.toString().trim().replaceAll("/\\*.*\\*/", "");
                AxisPts axisPts = buildA2lObject(axisPtsContent);
                axisPtsList.put(axisPts.getLabel(), axisPts);
            } catch (Exception e) {
                logger.error(ab.build().getLabel());
                e.printStackTrace();
            }
        }
    }

    private AxisPts buildA2lObject(String axisPtsContent) {
        String[] axisPtsParts = divideContent(axisPtsContent);
        ab.label(axisPtsParts[2]);
        ab.description(axisPtsParts[3]);
        ab.address(axisPtsParts[4]);
        ab.inputquantity(axisPtsParts[5]);
        ab.refRecordLayout(axisPtsParts[6]);
        ab.maxdiff(axisPtsParts[7]);
        ab.refComputMethod(axisPtsParts[8]);
        ab.nbpoints(axisPtsParts[9]);
        ab.lower(axisPtsParts[10]);
        ab.upper(axisPtsParts[11]);
        return ab.build();
    }

    /**
     * @return the axisPtsList
     */
    public Map<String, AxisPts> getAxisPtsList() {
        return axisPtsList;
    }

}
