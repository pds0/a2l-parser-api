package com.psagroup.calibrationparserapi.parser.a2l;

import com.psagroup.calibrationparserapi.a2lobject.axis.AxisDescrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AxisDescrParser {
    private final Logger logger = LoggerFactory.getLogger(AxisDescrParser.class);

    AxisDescrBuilder ab = new AxisDescrBuilder();

    void parseLR(String[] axisPart) {
        try {
            ab.type(axisPart[0]);
            ab.inputquantity(axisPart[1]);
            ab.refCompuMethod(axisPart[2]);
            ab.nbpoints(axisPart[3]);
            ab.lower(axisPart[4]);
            ab.upper(axisPart[5]);

            parseLRBottom(axisPart);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Parse with keywords what remains in the content after the mandatory fields
     *
     * @param axisPart Array containing the split String content of the A2lObject
     */
    private void parseLRBottom(String[] axisPart) {
        Integer i = 6;
        String element;
        while (i < axisPart.length) {
            switch (element = axisPart[i]) {
                case "FORMAT":
                    ab.format(element + " " + axisPart[i + 1]);
                    break;
                case "EXTENDED_LIMITS":
                    ab.extended_limits(element + " " + axisPart[i + 1] + " " + axisPart[i + 2]);
                    break;
                case "AXIS_PTS_REF":
                    ab.refaxispts(element + " " + axisPart[i + 1]);
                    break;
                case "FIX_AXIS_PAR":
                case "FIX_AXIS_PAR_DIST":
                    ab.fixaxispts(element + " " + axisPart[i + 1] + " " + axisPart[i + 2] + " " + axisPart[i + 3]);
                    break;
                default:
                    break;
            }
            i++;
        }
    }

}
