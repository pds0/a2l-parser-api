package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuMethod;
import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuMethodBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class CompuMethodParser extends A2LObjectParser {
    private final Logger logger = LoggerFactory.getLogger(CompuMethodParser.class);

    CompuMethodBuilder cb = new CompuMethodBuilder();
    final String a2lObjecttype = "COMPU_METHOD";
    private HashMap<String, CompuMethod> compList = new HashMap<>();

    public void parse(BufferedReader reader, String currentLine) throws IOException {
        if (currentLine.replaceAll("\\s+", "").startsWith("/beginCOMPU_METHOD")) {
            StringBuilder sb = new StringBuilder();
            sb.append(currentLine);
            while (!(currentLine = next(reader)).replaceAll("\\s+", "").startsWith("/endCOMPU_ME")) {
                // CompuMethod can have two sets of quotes following each other
                // so when there is no carriage return we put one "\n" between the two sets
                // to make it pass through the regex in divideContent method
                if (currentLine.contains("\" \"")) {
                    currentLine = currentLine.replace("\" \"", "\" \n \"");
                }
                sb.append("\n ");
                sb.append(currentLine);
            }
            try {
                String compContent = sb.toString().trim().replaceAll("/\\*.*\\*/", "");
                CompuMethod compuMethod = buildA2lObject(compContent);
                compList.put(compuMethod.getLabel(), compuMethod);
            } catch (Exception e) {
                logger.error(cb.build().getLabel());
                e.printStackTrace();
            }
        }
    }

    private CompuMethod buildA2lObject(String compContent) {
        String[] compParts = divideContent(compContent);
        cb.label(compParts[2]);
        cb.description(compParts[3]);
        cb.type(compParts[4]);
        cb.format(compParts[5]);
        cb.unit(compParts[6]);
        if (compParts[4].equals("RAT_FUNC")) {
            cb.coeffs(compParts[7] + " " + compParts[8] + " " + compParts[9] + " " + compParts[10] + " " + compParts[11] + " " + compParts[12] + " " + compParts[13]);
        } else if (compParts[4].equals("TAB_VERB")) {
            cb.refcomputab(compParts[7] + " " + compParts[8]);
        }

        return cb.build();
    }

    /**
     * @return the compList
     */
    public HashMap<String, CompuMethod> getCompList() {
        return compList;
    }

}
