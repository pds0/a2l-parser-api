package com.psagroup.calibrationparserapi.service;

import com.psagroup.calibrationparserapi.assignvalue.ValueHandler;
import com.psagroup.calibrationparserapi.parser.a2l.A2lParser;
import com.psagroup.calibrationparserapi.parser.hex.IntelHexException;
import com.psagroup.calibrationparserapi.parser.hex.ParserHex;
import com.psagroup.calibrationparserapi.parser.hex.listeners.RangeDetector;
import com.psagroup.calibrationparserapi.parser.ulp.ParserUlp;
import com.psagroup.calibrationparserapi.parser.ulp.SRecordParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ParserService {
    private final Logger logger = LoggerFactory.getLogger(ParserService.class);

    //All the Parsers that can be instantiated (if useful)
    private A2lParser pA2l;
    private ParserHex pHex;
    private ParserUlp pUlp;

    public ValueHandler vh;


    public void parseHexFile(File ulpFile) {
        pUlp = null;

        //start
        long lStartTime = System.nanoTime();

        // Parsing of the IntelHex file (and transfer of the data record with their respective start
        // addresses in the HashMap pHex.addressMap)
        try (FileInputStream isHex = new FileInputStream(ulpFile)) {
            logger.info("Start parsing the IntelHex file: ");
            pHex = new ParserHex(isHex);
            RangeDetector rangeDetector = new RangeDetector();
            pHex.setDataListener(rangeDetector);
            pHex.parse();
            logger.info("End parsing of the IntelHex: ");
        } catch (IntelHexException | IOException e) {
            e.printStackTrace();
        }


        //end
        long lEndTime = System.nanoTime();

        //time elapsed
        long output = lEndTime - lStartTime;
        logger.info("Hex parsing time in miliseconds: " + output / 1000000);

    }

    public void parseUlpFile(File ulpFile) {
        pHex = null;
        //start
        long lStartTime = System.nanoTime();

        // Parsing of the EAGLE SRecord file (and transfer of the data record with their respective start
        // addresses in the HashMap pUlp.addressMap)
        try (FileInputStream isUlp = new FileInputStream(ulpFile)) {
            logger.info("Start parsing the SRecord file: ");
            pUlp = new ParserUlp(isUlp);
            pUlp.parse();
            logger.info("End parsing of the SRecord: ");
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (SRecordParseException e) {
            logger.error(e.getMessage());
        }


        //end
        long lEndTime = System.nanoTime();

        //time elapsed
        long output = lEndTime - lStartTime;
        logger.info("Ulp parsing time in miliseconds: " + output / 1000000);
    }

    /**
     * Main function for parsing the 2 files and assign values to Characs
     *
     * @param a2lFile
     */
    public void parseA2LFile(File a2lFile) {

        //start
        long lStartTime = System.nanoTime();

        // Parsing of the A2l file and creation of the A2lObjects (Charac, Compu_Method, RecordLayout)
        try (FileInputStream isA2l = new FileInputStream(a2lFile)) {
            pA2l = new A2lParser(isA2l);
            logger.info("Lancement du parsage du ficher A2L");
            pA2l.parse();
            logger.info("Fin du parsage du ficher A2L");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //end
        long lEndTime = System.nanoTime();

        //time elapsed
        long output = lEndTime - lStartTime;
        logger.info("A2l parsing time in miliseconds: " + output / 1000000);
    }


    public void assignValues(List<String> labels, Map<String, String> recordData) {
        logger.info("Demarrage de l'affectation des resultats pour les Characteristics");

        // After the creation of all of the A2l objects, we assign values to Charac with the
        // ValueHandler
        vh = new ValueHandler(pA2l.caracList, recordData, pA2l.axisPtsMap, pA2l.compuVTabMap,
                pA2l.compuMethodMap, pA2l.recordLayoutMap, labels, getTypeRecordData());

        try {
            vh.assignValues(getTypeRecordData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("Fin de l'affectation des resultats pour les Characteristics");

    }

    public Map<String, String> getRecordData() {
        if (pHex != null) return pHex.getAddressMap();
        if (pUlp != null) return pUlp.getRecords();
        return null;
    }

    private Integer getTypeRecordData() {
        if (pHex != null) return 0;
        if (pUlp != null) return 1;
        return null;
    }


}
