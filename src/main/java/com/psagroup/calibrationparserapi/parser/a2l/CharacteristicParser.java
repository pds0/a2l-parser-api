package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.characteristic.Characteristic;
import com.psagroup.calibrationparserapi.a2lobject.characteristic.CharacteristicBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

public class CharacteristicParser extends A2LObjectParser {
    private CharacteristicBuilder cb = new CharacteristicBuilder();
    private LinkedList<Characteristic> caracList = new LinkedList<>();

    public void parse(BufferedReader reader, String currentLine) throws IOException {
        if (currentLine.replaceAll("\\s+", "").startsWith("/beginCHARACTERISTIC")) {
            StringBuilder sb = new StringBuilder();
            sb.append(currentLine);
            while (!(currentLine = next(reader)).replaceAll("\\s+", "").startsWith("/endCHARAC")) {
                sb.append("\n ");
                sb.append(currentLine);
            }
            try {
                String charContent = sb.toString().trim().replaceAll("/\\*.*\\*/", "");
                caracList.add(buildA2lObject(charContent));
            } catch (Exception e) {
                System.out.println(cb.build().getLabel());
                e.printStackTrace();
            }

        }
    }

    public Characteristic buildA2lObject(String charContent) {
        String[] charParts = divideContent(charContent);
        cb.label(charParts[2]);
        cb.description(charParts[3]);
        cb.charType(charParts[4]);
        cb.address(charParts[5]);
        cb.refRecordLayout(charParts[6]);
        cb.maxdiff(charParts[7]);
        cb.refCompuMethod(charParts[8]);
        cb.lower(charParts[9]);
        cb.upper(charParts[10]);

        parseBottom(charParts);
        return cb.build();
    }

    /**
     * Parse with keywords what remains in the content after the mandatory fields
     *
     * @param charParts Array containing the split String content of the A2lObject
     */
    private void parseBottom(String[] charParts) {
        Integer i = 11;
        String element;
        while (i < charParts.length) {
            switch (element = charParts[i]) {
                case "FORMAT":
                    cb.format(element + " " + charParts[i + 1]);
                    break;
                case "EXTENDED_LIMITS":
                    cb.extended_limits(element + " " + charParts[i + 1] + " " + charParts[i + 2]);
                    break;
                case "NUMBER":
                    cb.number(element + " " + charParts[i + 1]);
                    break;
                case "MATRIX_DIM":
                    cb.number(element + " " + charParts[i + 1] + " " + charParts[i + 2] + " " + charParts[i + 3]);
                    break;
                case "/begin":
                    if (charParts[i + 1].equals("AXIS_DESCR")) {
                        int j = i + 2;
                        StringBuilder axisSb = new StringBuilder();
                        while (!charParts[j].equals("/end")) {
                            axisSb.append(charParts[j]);
                            axisSb.append("\n");
                            j++;
                        }
                        AxisDescrParser pAxisDesr = new AxisDescrParser();
                        pAxisDesr.parseLR(divideContent(axisSb.toString().trim()));
                        cb.addAxis(pAxisDesr.ab.build());
                    }
                    break;
                default:
                    break;
            }
            i++;
        }
    }

    /**
     * @return the caracList
     */
    public LinkedList<Characteristic> getCaracList() {
        return caracList;
    }

}
