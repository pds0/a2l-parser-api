package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuVTab;
import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuVTabBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class CompuVTabParser extends A2LObjectParser {
    private final Logger logger = LoggerFactory.getLogger(CompuVTabParser.class);

    private CompuVTabBuilder cb = new CompuVTabBuilder();
    private static final String a2lObjecttype = "COMPU_VTAB";
    private HashMap<String, CompuVTab> compVTabList = new HashMap<>();


    public void parse(BufferedReader reader, String currentLine) throws IOException {
        if (currentLine.replaceAll("\\s+", "").startsWith("/beginCOMPU_VTAB")) {
            StringBuilder sb = new StringBuilder();
            sb.append(currentLine);
            while (!(currentLine = next(reader)).replaceAll("\\s+", "").startsWith("/endCOMPU_V")) {
                sb.append("\n ");
                sb.append(currentLine);
            }

            try {
                String compVTabContent = sb.toString().trim().replaceAll("/\\*.*\\*/", "");
                CompuVTab compuVTab = buildA2lObject(compVTabContent);
                this.compVTabList.put(compuVTab.getLabel(), compuVTab);

            } catch (Exception e) {
                logger.error(cb.build().getLabel());
                e.printStackTrace();
            }


        }
    }

    private CompuVTab buildA2lObject(String compVTabContent) throws IOException {
        String[] compVTabParts = divideContent(compVTabContent);

        cb.label(compVTabParts[2]);
        cb.description(compVTabParts[3]);
        cb.type(compVTabParts[4]);
        String nbofStrings = compVTabParts[5];
        cb.nbofStrings(nbofStrings);
        for (int i = 6; i < 6 + Integer.parseInt(nbofStrings) * 2; i = i + 2) {
            if (compVTabParts[i].trim().startsWith("/end")) {
                break;
            }
            cb.enuLR(Arrays.copyOfRange(compVTabParts, i, i + 2));
        }
        return cb.build();
    }

    /**
     * @return the compVTabList
     */
    HashMap<String, CompuVTab> getCompVTabList() {
        return compVTabList;
    }

}
