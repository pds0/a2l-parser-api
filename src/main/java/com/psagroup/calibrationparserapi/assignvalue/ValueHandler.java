package com.psagroup.calibrationparserapi.assignvalue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.psagroup.calibrationparserapi.a2lobject.axis.AxisDescr;
import com.psagroup.calibrationparserapi.a2lobject.axis.AxisPts;
import com.psagroup.calibrationparserapi.a2lobject.characteristic.Characteristic;
import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuMethod;
import com.psagroup.calibrationparserapi.a2lobject.compumethod.CompuVTab;
import com.psagroup.calibrationparserapi.a2lobject.recordlayout.DataType;
import com.psagroup.calibrationparserapi.a2lobject.recordlayout.RecordLayout;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ValueHandler {

    private static final String AXIS_Y = "axisY";
    private static final String AXIS_X = "axisX";
    private static final String UNIT = "unit";
    private static final String READWAY = "readway";
    private static final String VALUE_FIELD = "value";
    private static final String ROW_DIR = "ROW_DIR";
    private static final String COLUMN_DIR = "COLUMN_DIR";
    private static final String DATA_FIELD = "data";
    public static final int ULP_DATA_RANGE = 127;
    public static final int HEX_DATA_RANGE = 32;

    private final Logger logger = LoggerFactory.getLogger(ValueHandler.class);

    private List<Characteristic> caracList;
    private Map<String, String> addressMap;
    private Map<String, RecordLayout> recordlayoutMap;
    private Map<String, CompuMethod> compuMethodMap;
    private Map<String, CompuVTab> compuVTabMap;
    private Map<String, AxisPts> axisPtsMap;
    public List<Characteristic> filteredCaracList;
    private List<String> filterCarac;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private Integer dataRangeRecord;

    public ValueHandler(List<Characteristic> caracList, Map<String, String> addressMap,
                        Map<String, AxisPts> axisPtsMap, Map<String, CompuVTab> compuVTabMap,
                        Map<String, CompuMethod> compuMethodMap, Map<String, RecordLayout> recordlayoutMap,
                        List<String> filterCarac, Integer recordType) {
        this.caracList = caracList;
        this.addressMap = addressMap;
        this.axisPtsMap = axisPtsMap;
        this.recordlayoutMap = recordlayoutMap;
        this.compuMethodMap = compuMethodMap;
        this.compuVTabMap = compuVTabMap;
        this.filterCarac = filterCarac;
        this.filteredCaracList = new LinkedList<>();
        setDataRecordRange(recordType);
    }

    private void setDataRecordRange(Integer recordType) {
        if (recordType == 0) {
            this.dataRangeRecord = HEX_DATA_RANGE;
        } else if (recordType == 1) {
            this.dataRangeRecord = ULP_DATA_RANGE;
        }
    }

    public List<Characteristic> assignValues(Integer recordType) {
        setDataRecordRange(recordType);


        for (Iterator<Characteristic> iterator = caracList.iterator(); iterator.hasNext(); ) {
            Characteristic c = iterator.next();

            if (filterCarac.contains(c.getLabel()) || filterCarac.isEmpty()) {
                if (c.getChartype() == null) {
                    // If the Chartype is unknown by the system
                    logger.error("Type de CharType inconnu {} pour la characteristic {}", c.getChartype(), c.getLabel());
                } else {
                    // For all types of Values (Simple, Set of values, curve, map), we handle their
                    // assignment of values in different ways
                    try {
                        switch (c.getChartype()) {
                            case VALUE:
                                handleSingleValue(c);
                                break;
                            case VAL_BLK:
                                handleValBlkValue(c);
                                break;
                            case CURVE:
                                handleCurveValue(c);
                                break;
                            case MAP:
                                handleMapValue(c);
                                break;
                            default:
                                // If the Chartype is known but just not handled yet
                                logger.warn("Type de CharType pas encore implementé {}", c.getChartype());
                                break;
                        }
                    } catch (Exception e) {
                        logger.error(c.toString());
                        e.printStackTrace();
                    }
                    filteredCaracList.add(c);
                }
            }
        }

        return filteredCaracList;

    }

    /**
     * Handle the simple value charac assignment
     *
     * @param c the charac to assign
     */
    private void handleSingleValue(Characteristic c) {
        // Depending of the ComputMethod of the Charac, it's either a numeric value or a
        // alphanumeric value, we handle those cases in different ways
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                setSinglevalueNum(c);
                break;
            case TAB_VERB:
                setSinglevalueEnum(c);
                break;

            default:
                break;
        }
    }

    /**
     * Assign a simple numeric value to the Charac
     *
     * @param c the charac to assign
     */
    private void setSinglevalueNum(Characteristic c) {
        // Determine the number of hexadecimal byte to read to get a single value
        DataType datatype = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue();
        int byteperRead = datatype.getNbbits() / 8;
        ObjectNode data = jsonMapper.createObjectNode();
        ObjectNode value = data.putObject(DATA_FIELD);
        try {
            String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");
            // Fetch the data of the value in the IntelHex AddressMap then apply the
            // function from the COMPU_METHOD
            value.put(VALUE_FIELD, compuMethodMap.get(c.getRefComputMethod())
                    .rat_func(decodeValue(datatype, addressMap, byteperRead, c.getAddress()))).put(UNIT, unit);
        } catch (Exception e) {
            logger.warn("Pour l'adresse {} de la charac {}", c.getAddress(), c.getLabel());
        }
        c.setResult(data);
    }

    /**
     * Assign a simple alphanumeric value to the Charac from an enumeration
     *
     * @param c the charac to assign
     */
    private void setSinglevalueEnum(Characteristic c) {
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
        ObjectNode data = jsonMapper.createObjectNode();
        ObjectNode value = data.putObject(DATA_FIELD);
        try {
            value.put(VALUE_FIELD,
                    enuObj.getEnu().get(decodePosValue(addressMap, byteperRead, c.getAddress())).replaceAll("\"", ""))
                    .put(UNIT, "");
        } catch (Exception e) {
            logger.warn("Impossible d'assigner la valeur énumeré (pour l'adresse : {}) de la characteristic {}",
                    c.getAddress(), c.getLabel());
        }
        c.setResult(data);
    }

    /**
     * Handle the Set of values Charac assignment
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleValBlkValue(Characteristic c) {
        String address = c.getAddress();
        ObjectNode data = jsonMapper.createObjectNode();
        ObjectNode value = data.putObject(DATA_FIELD);
        ArrayNode objtab = jsonMapper.createArrayNode();
        int nbpoints = c.getNumber();
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        CompuMethod comp = compuMethodMap.get(c.getRefComputMethod());
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = comp.getUnit().replaceAll("\"", "");
                double[] dtab = affectValBlkNum(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), address,
                        nbpoints, byteperRead, comp);
                for (int i = 0; i < dtab.length; i++) {
                    objtab.add(dtab[i]);
                }

                value.set(VALUE_FIELD, objtab);
                value.put(UNIT, unit);
                c.setResult(data);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectValBlkEnum(address, nbpoints, byteperRead, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtab.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                value.set(VALUE_FIELD, objtab);
                value.put(UNIT, "");
                c.setResult(data);

                break;
            default:
                break;
        }
        c.setResult(data);
    }

    /**
     * Assign a set of alphanumeric values to the Charac from an enumeration
     *
     * @param startaddress the address where to begin to fetch the data from
     * @param nbpoints     the number of values to fetch
     * @param byteperRead  number of byte which a single value need to be fetched
     * @param enuObj       the COMPVTAB where the enumeration is
     * @return an array of String containing the set of values of String
     */
    private String[] affectValBlkEnum(String startaddress, int nbpoints, int byteperRead, CompuVTab enuObj) {
        String[] stab = new String[nbpoints];
        String hexAddrAbs = "";
        for (int i = 0; i < nbpoints; i++) {
            hexAddrAbs = "0x" + Long.toHexString(Long.decode(startaddress) + i * byteperRead);
            stab[i] = enuObj.getEnu().get(decodePosValue(addressMap, byteperRead, hexAddrAbs));

        }
        return stab;
    }

    /**
     * Assign a set of numeric values to the Charac
     *
     * @param d            the datatype of the values being fetched
     * @param startaddress the address where to begin to fetch the data from
     * @param nbpoints     the number of values to fetch
     * @param byteperRead  number of byte which a single value need to be fetched
     * @param comp         COMPU_METHOD of the values being fetched
     * @return an array of numeric values
     */
    private double[] affectValBlkNum(DataType d, String startaddress, int nbpoints, int byteperRead, CompuMethod comp) {
        double[] dtab = new double[nbpoints];
        String hexAddrData = "";
        for (int i = 0; i < nbpoints; i++) {
            hexAddrData = "0x" + Long.toHexString(Long.decode(startaddress) + i * byteperRead);
            dtab[i] = comp.rat_func(decodeValue(d, addressMap, byteperRead, hexAddrData));
        }
        return dtab;
    }

    /**
     * Handle the CURVE Charac assignment
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleCurveValue(Characteristic c) {
        switch (c.getAxises().getFirst().getType()) {
            case STD_AXIS:
                handleSTDCurve(c);
                break;
            case COM_AXIS:
                handleCOMCurve(c);
                break;
            case FIX_AXIS:
                handleFIXCurve(c);
                break;
            default:
                break;
        }
    }

    /**
     * Handle the CURVE Charac assignment with a FIX axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleFIXCurve(Characteristic c) {
        AxisDescr axis = c.getAxises().getFirst();
        int nbpoints = axis.getNbpoints();
        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxis = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        double[] fixaxispar = axis.getFixAxisPar();

        // Determination of the axis
        switch (compuMethodMap.get((axis.getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                CompuMethod comp = compuMethodMap.get(axis.getRefCompuMethod());
                String unit = comp.getUnit().replaceAll("\"", "");

                double[] dtab = affectFIXAxisNum(comp, nbpoints, fixaxispar);
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxis.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(axis.getRefCompuMethod()).getRefcomputab()));
                String[] stab = affectFIXAxisEnum(nbpoints, fixaxispar, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxis.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));

                }
                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the Data values
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        String hexAddrData = c.getAddress();
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), hexAddrData,
                        nbpoints, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints, hexAddrData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, "");

                break;
            default:
                break;
        }
        c.setResult(main);
    }

    /**
     * Determine an alphanumeric FIX axis
     *
     * @param nbpoints   the number of values contained by the FIX axis
     * @param fixaxispar the increment used for determining a FIX axis
     * @param enuObj     the COMPVTAB where the enumeration is
     * @return an array of String containing the set of values of String
     */
    private String[] affectFIXAxisEnum(int nbpoints, double[] fixaxispar, CompuVTab enuObj) {
        String[] stab = new String[nbpoints];
        for (int i = 0; i < nbpoints; i++) {
            stab[i] = enuObj.getEnu().get(fixaxispar[0] + i * fixaxispar[1]);
        }
        return stab;
    }

    /**
     * Determine a numeric FIX axis
     *
     * @param comp       COMPU_METHOD of the values being fetched
     * @param nbpoints   the number of values contained by the FIX axis
     * @param fixaxispar the increment used for determining a FIX axis
     * @return a array of numeric values representing a FIX axis
     */
    private double[] affectFIXAxisNum(CompuMethod comp, int nbpoints, double[] fixaxispar) {
        double[] atab = new double[nbpoints];
        for (int i = 0; i < nbpoints; i++) {
            atab[i] = comp.rat_func(fixaxispar[0] + i * fixaxispar[1]);
        }
        return atab;
    }

    /**
     * Handle the CURVE Charac assignment with a COM axis
     *
     * @param c the charac to assign
     */
    private void handleCOMCurve(Characteristic c) {

        AxisDescr axis = c.getAxises().getFirst();
        int nbpoints = axis.getNbpoints();
        AxisPts axisPts = axisPtsMap.get(axis.getRefAxisPts());
        int byteperReadAxe = recordlayoutMap.get(axisPts.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        int byteperReadNoAxe = (recordlayoutMap.get(axisPts.getRefRecordLayout()).getNoaxisptsx() == null) ? 0
                : recordlayoutMap.get(axisPts.getRefRecordLayout()).getNoaxisptsx().getNbbits() / 8;
        String hexAddrAbs = "0x" + Long.toHexString(Long.decode(axisPts.getAddress()) + byteperReadNoAxe);

        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxis = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determination of the axis
        switch (compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(axis.getRefCompuMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(axisPts.getRefRecordLayout()).getAxisptsx(), hexAddrAbs,
                        nbpoints, byteperReadAxe, compuMethodMap.get(axis.getRefCompuMethod()));

                for (int i = 0; i < dtab.length; i++) {
                    objtabaxis.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().getFirst().getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectCOMEnum(byteperReadAxe, nbpoints, hexAddrAbs, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxis.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the Data values
        String hexAddrData = c.getAddress();
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), hexAddrData,
                        nbpoints, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints, hexAddrData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    if (stab[i] != null) {
                        objtabdata.add(stab[i].replaceAll("\"", ""));
                    } else {
                        objtabdata.add(stab[i].replaceAll("\"", ""));
                    }
                }
                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, "");
                break;
            default:
                break;
        }
        c.setResult(main);
    }

    /**
     * Handle the CURVE Charac assignment with a STD axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleSTDCurve(Characteristic c) {
        int byteperReadAxe = recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        int byteperReadNoAxe = (recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsx() == null) ? 0
                : recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsx().getNbbits() / 8;
        String address = c.getAddress();
        boolean isStatic = (recordlayoutMap.get(c.getRefRecordLayout()).getLabel().endsWith("_Static"));
        int nbpoints = (int) Math.min(decodeValue(recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsx(),
                addressMap, byteperReadNoAxe, address), c.getAxises().getFirst().getNbpoints());
        // If the value is negative get the value stored in the characteristic
        nbpoints = (nbpoints <= 0) ? c.getAxises().getFirst().getNbpoints() : nbpoints;

        String hexAddrAbs = "0x" + Long.toHexString(Long.decode(c.getAddress()) + byteperReadNoAxe);

        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxis = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determine the axis
        switch (compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getUnit().replaceAll("\"",
                        "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsx(), hexAddrAbs,
                        nbpoints, byteperReadAxe, compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxis.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, unit);

                break;
            case TAB_VERB:

                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().getFirst().getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectSTDEnum(byteperReadAxe, nbpoints, hexAddrAbs, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxis.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxis);
                axisX.put(UNIT, "");

                break;
            default:
                break;
        }

        String startHexAddrData = shiftForStatic(isStatic, address, hexAddrAbs, byteperReadNoAxe, byteperReadAxe,
                nbpoints);
        // Determine the Data values
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:

                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), startHexAddrData,
                        nbpoints, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));

                String[] stab = affectSTDEnum(byteperRead, nbpoints, startHexAddrData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add(stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, "");

                break;
            default:
                break;
        }

        c.setResult(main);
    }

    /**
     * Shift the range of read for the Data in the Address Map if it's a Static
     * record and the axis and data are distributed in severals lines
     *
     * @param isStatic       if the record Layout of the charac is a Static Record Layout
     * @param address        the address of the no axis (so the beginning of the range to read)
     * @param hexAddrAbs     the address after the axis
     * @param bytePerRead    bytePerRead of Data
     * @param bytePerReadAxe bytePerRead of Data
     * @param nbPoints       the number of data and axis points
     * @return the new address for reading the Data
     */
    private String shiftForStatic(boolean isStatic, String address, String hexAddrAbs, int bytePerRead,
                                  int bytePerReadAxe, int nbPoints) {
        if (isStatic) {
            long beginDecAddress = Long.decode(address);
            long endDecAddress = Long.decode(hexAddrAbs) + (bytePerReadAxe + bytePerRead) * nbPoints;
            String beginRange = "";
            String endRange = "";
            for (long i = beginDecAddress - dataRangeRecord; i < beginDecAddress; i++) {
                if (addressMap.containsKey(Long.toHexString(i))) {
                    beginRange = Long.toHexString(i);
                }
            }
            for (long i = endDecAddress - dataRangeRecord; i < endDecAddress; i++) {
                if (addressMap.containsKey(Long.toHexString(i))) {
                    endRange = Long.toHexString(i);
                }
            }

            if (!beginRange.equals(endRange)) {
                return "0x" + Long.toHexString(Long.decode(hexAddrAbs) + bytePerReadAxe * nbPoints + 1);
            }

        }
        return "0x" + Long.toHexString(Long.decode(hexAddrAbs) + bytePerReadAxe * nbPoints);
    }

    /**
     * Determine an alphanumeric STD axis
     *
     * @param byteperRead
     * @param nbpoints
     * @param startHexAddrData
     * @param enuObj
     * @return an array of String containing the set of values of String
     */
    private String[] affectSTDEnum(int byteperRead, int nbpoints, String startHexAddrData, CompuVTab enuObj) {
        String[] stab = new String[nbpoints];
        String hexAddrAbs = "";
        for (int i = 0; i < nbpoints; i++) {
            hexAddrAbs = "0x" + Long.toHexString(Long.decode(startHexAddrData) + i * byteperRead + byteperRead);
            stab[i] = enuObj.getEnu().get(decodePosValue(addressMap, byteperRead, hexAddrAbs));
        }
        return stab;
    }

    /**
     * Determine an alphanumeric COM axis
     *
     * @param byteperRead
     * @param nbpoints
     * @param startHexAddrData
     * @param enuObj
     * @return an array of String containing the set of values of String
     */
    private String[] affectCOMEnum(int byteperRead, int nbpoints, String startHexAddrData, CompuVTab enuObj) {
        String[] stab = new String[nbpoints];
        String hexAddrAbs = "";
        for (int i = 0; i < nbpoints; i++) {
            hexAddrAbs = "0x" + Long.toHexString(Long.decode(startHexAddrData) + i * byteperRead);
            double decodePosValue = decodePosValue(addressMap, byteperRead, hexAddrAbs);
            stab[i] = enuObj.getEnu().get(decodePosValue);
            if (stab[i] == null) {
                logger.warn("Valeur inexistante pour la clé {}  dans l'énumeration {}", decodePosValue,
                        enuObj.getLabel());
                stab[i] = Double.toString(decodePosValue);
            }
        }
        return stab;
    }

    /**
     * Handle the MAP Charac assignment
     *
     * @param c the charac to assign
     */
    private void handleMapValue(Characteristic c) {
        switch (c.getAxises().getFirst().getType()) {
            case STD_AXIS:
                switch (c.getAxises().get(1).getType()) {
                    case STD_AXIS:
                        handleSTDSTDMap(c);
                        break;
                    case COM_AXIS:
                        // NOT YET IMPLEMENTED
                        logger.debug("STD_AXIS et COM_AXIS {}", c);
                        break;
                    case FIX_AXIS:
                        // NOT YET IMPLEMENTED
                        logger.debug("STD_AXIS et FIX_AXIS {}", c);
                        break;
                    default:
                        break;
                }
                break;

            case COM_AXIS:
                switch (c.getAxises().get(1).getType()) {
                    case STD_AXIS:
                        // NOT YET IMPLEMENTED
                        logger.debug("COM_AXIS et STD_AXIS {}", c);
                        break;
                    case COM_AXIS:
                        logger.debug("COM_AXIS et COM_AXIS {}", c);
                        handleCOMCOMMap(c);
                        break;
                    case FIX_AXIS:
                        handleCOMFIXMap(c);
                        break;
                    default:
                        break;
                }

                break;

            case FIX_AXIS:
                switch (c.getAxises().get(1).getType()) {
                    case STD_AXIS:
                        // NOT YET IMPLEMENTED
                        logger.debug("FIX_AXIS et STD_AXIS {}", c);
                        break;
                    case COM_AXIS:
                        // NOT YET IMPLEMENTED
                        logger.debug("FIX_AXIS et COM_AXIS {}", c);
                        break;
                    case FIX_AXIS:
                        handleFIXFIXMap(c);
                        break;
                    default:
                        break;
                }
                break;

            default:
                logger.warn("Type de Map non reconnue");
                break;
        }
    }

    /**
     * Handle the MAP Charac assignment with a COM axis and a FIX axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleCOMFIXMap(Characteristic c) {
        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode axisY = main.putObject(AXIS_Y);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxisX = jsonMapper.createArrayNode();
        ArrayNode objtabaxisY = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determine the axis 1
        AxisPts axisPts1 = axisPtsMap.get(c.getAxises().getFirst().getRefAxisPts());
        int nbpoints1 = axisPts1.getNbpoints();
        int byteperReadAxe1 = recordlayoutMap.get(axisPts1.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        String addressAxe1 = axisPts1.getAddress();

        switch (compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getUnit().replaceAll("\"",
                        "");

                double[] dtab = affectNumValues(recordlayoutMap.get(axisPts1.getRefRecordLayout()).getAxisptsx(),
                        addressAxe1, nbpoints1, byteperReadAxe1,
                        compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisX.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().getFirst().getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectCOMEnum(byteperReadAxe1, nbpoints1, addressAxe1, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisX.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, "");

                break;
            default:
                break;
        }

        // Determine the axis 1
        AxisDescr axis2 = c.getAxises().get(1);
        int nbpoints2 = axis2.getNbpoints();
        double[] fixaxispar2 = axis2.getFixAxisPar();
        switch (compuMethodMap.get((axis2.getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                CompuMethod comp2 = compuMethodMap.get(axis2.getRefCompuMethod());
                String unit = comp2.getUnit().replaceAll("\"", "");

                double[] dtab = affectFIXAxisNum(comp2, nbpoints2, fixaxispar2);
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisY.add(dtab[i]);
                }

                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj2 = compuVTabMap.get((compuMethodMap.get(axis2.getRefCompuMethod()).getRefcomputab()));
                String[] stab = affectFIXAxisEnum(nbpoints2, fixaxispar2, enuObj2);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisY.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine of the data
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        String addressData = c.getAddress();
        String readway = recordlayoutMap.get(c.getRefRecordLayout()).isRowdir() ? ROW_DIR : COLUMN_DIR;

        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), addressData,
                        nbpoints1 * nbpoints2, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);
                data.put(READWAY, readway);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints1 * nbpoints2, addressData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabaxisY);
                data.put(UNIT, "");
                data.put(READWAY, readway);

                break;
            default:
                break;
        }
        c.setResult(main);
    }

    /**
     * Handle the MAP Charac assignment with a FIX axis and a FIX axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleFIXFIXMap(Characteristic c) {
        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode axisY = main.putObject(AXIS_Y);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxisX = jsonMapper.createArrayNode();
        ArrayNode objtabaxisY = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determine the axis 1
        AxisDescr axis1 = c.getAxises().getFirst();
        int nbpoints1 = axis1.getNbpoints();
        double[] fixaxispar1 = axis1.getFixAxisPar();

        switch (compuMethodMap.get((axis1.getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                CompuMethod comp1 = compuMethodMap.get(axis1.getRefCompuMethod());

                String unit = comp1.getUnit().replaceAll("\"", "");

                double[] dtab = affectFIXAxisNum(comp1, nbpoints1, fixaxispar1);
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisX.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj1 = compuVTabMap.get((compuMethodMap.get(axis1.getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectFIXAxisEnum(nbpoints1, fixaxispar1, enuObj1);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisX.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the axis 2
        AxisDescr axis2 = c.getAxises().get(1);
        int nbpoints2 = axis2.getNbpoints();
        double[] fixaxispar2 = axis2.getFixAxisPar();

        switch (compuMethodMap.get((axis2.getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                CompuMethod comp2 = compuMethodMap.get(axis2.getRefCompuMethod());
                String unit = comp2.getUnit().replaceAll("\"", "");

                double[] dtab = affectFIXAxisNum(comp2, nbpoints2, fixaxispar2);
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisY.add(dtab[i]);
                }

                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj2 = compuVTabMap.get((compuMethodMap.get(axis2.getRefCompuMethod()).getRefcomputab()));
                String[] stab = affectFIXAxisEnum(nbpoints2, fixaxispar2, enuObj2);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisY.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the data
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        String addressData = c.getAddress();
        String readway = recordlayoutMap.get(c.getRefRecordLayout()).isRowdir() ? ROW_DIR : COLUMN_DIR;

        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), addressData,
                        nbpoints1 * nbpoints2, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);
                data.put(READWAY, readway);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints1 * nbpoints2, addressData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabaxisY);
                data.put(UNIT, "");
                data.put(READWAY, readway);
                break;
            default:
                break;
        }

        c.setResult(main);
    }

    /**
     * Handle the MAP Charac assignment with a COM axis and a COM axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleCOMCOMMap(Characteristic c) {
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        AxisPts axisPts1 = axisPtsMap.get(c.getAxises().getFirst().getRefAxisPts());
        int byteperReadNoAxe1 = (recordlayoutMap.get(axisPts1.getRefRecordLayout()).getNoaxisptsx() == null) ? 0
                : recordlayoutMap.get(axisPts1.getRefRecordLayout()).getNoaxisptsx().getNbbits() / 8;
        int byteperReadAxe1 = recordlayoutMap.get(axisPts1.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        AxisPts axisPts2 = axisPtsMap.get(c.getAxises().get(1).getRefAxisPts());
        int byteperReadNoAxe2 = (recordlayoutMap.get(axisPts2.getRefRecordLayout()).getNoaxisptsx() == null) ? 0
                : recordlayoutMap.get(axisPts2.getRefRecordLayout()).getNoaxisptsx().getNbbits() / 8;
        int byteperReadAxe2 = recordlayoutMap.get(axisPts2.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        String addressData = c.getAddress();
        int nbpoints1 = axisPts1.getNbpoints();
        int nbpoints2 = axisPts2.getNbpoints();
        String addressAxe1 = "0x" + Long.toHexString(Long.decode(axisPts1.getAddress()) + byteperReadNoAxe1);
        String addressAxe2 = "0x" + Long.toHexString(Long.decode(axisPts2.getAddress()) + byteperReadNoAxe2);

        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode axisY = main.putObject(AXIS_Y);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxisX = jsonMapper.createArrayNode();
        ArrayNode objtabaxisY = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determine the axis 1
        switch (compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getUnit().replaceAll("\"",
                        "");

                double[] dtab = affectNumValues(recordlayoutMap.get(axisPts1.getRefRecordLayout()).getAxisptsx(),
                        addressAxe1, nbpoints1, byteperReadAxe1,
                        compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisX.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().getFirst().getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectCOMEnum(byteperReadAxe1, nbpoints1, addressAxe1, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisX.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the axis 2
        switch (compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(axisPts2.getRefRecordLayout()).getAxisptsx(),
                        addressAxe2, nbpoints2, byteperReadAxe2,
                        compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisY.add(dtab[i]);
                }

                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().get(1).getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectCOMEnum(byteperReadAxe2, nbpoints2, addressAxe2, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisY.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, "");

                break;
            default:
                break;
        }

        // Determine the Data
        String readway = recordlayoutMap.get(c.getRefRecordLayout()).isRowdir() ? ROW_DIR : COLUMN_DIR;
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), addressData,
                        nbpoints1 * nbpoints2, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);
                data.put(READWAY, readway);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints1 * nbpoints2, addressData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabaxisY);
                data.put(UNIT, "");
                data.put(READWAY, readway);
                break;
            default:
                break;
        }
        c.setResult(main);
    }

    /**
     * Handle the MAP Charac assignment with a STD axis and a STD axis
     *
     * @param c the charac to assign
     * @throws JSONException
     */
    private void handleSTDSTDMap(Characteristic c) {
        int byteperRead = recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue().getNbbits() / 8;
        int byteperReadNoAxe1 = (recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsx() == null) ? 0
                : recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsx().getNbbits() / 8;
        int byteperReadAxe1 = recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsx().getNbbits() / 8;
        int byteperReadNoAxe2 = (recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsy() == null) ? 0
                : recordlayoutMap.get(c.getRefRecordLayout()).getNoaxisptsy().getNbbits() / 8;
        int byteperReadAxe2 = recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsy().getNbbits() / 8;
        String address = c.getAddress();
        int nbpoints1 = c.getAxises().getFirst().getNbpoints();
        int nbpoints2 = c.getAxises().get(1).getNbpoints();
        String addressAxe1 = "0x" + Long.toHexString(Long.decode(address) + byteperReadNoAxe1 + byteperReadNoAxe2);
        String addressAxe2 = "0x" + Long.toHexString(Long.decode(addressAxe1) + byteperReadAxe1 * nbpoints1);
        String addressData = "0x" + Long.toHexString(Long.decode(addressAxe2) + byteperReadAxe2 * nbpoints2);

        ObjectNode main = jsonMapper.createObjectNode();
        ObjectNode axisX = main.putObject(AXIS_X);
        ObjectNode axisY = main.putObject(AXIS_Y);
        ObjectNode data = main.putObject(DATA_FIELD);
        ArrayNode objtabaxisX = jsonMapper.createArrayNode();
        ArrayNode objtabaxisY = jsonMapper.createArrayNode();
        ArrayNode objtabdata = jsonMapper.createArrayNode();

        // Determine the axis 1
        switch (compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())).getUnit().replaceAll("\"",
                        "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsx(), addressAxe1,
                        nbpoints1, byteperReadAxe1, compuMethodMap.get((c.getAxises().getFirst().getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisX.add(dtab[i]);
                }

                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, unit);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().getFirst().getRefCompuMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperReadAxe1, nbpoints1, addressAxe1, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisX.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisX.set(VALUE_FIELD, objtabaxisX);
                axisX.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the axis 2
        switch (compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getAxisptsy(), addressAxe2,
                        nbpoints2, byteperReadAxe2, compuMethodMap.get((c.getAxises().get(1).getRefCompuMethod())));
                for (int i = 0; i < dtab.length; i++) {
                    objtabaxisY.add(dtab[i]);
                }

                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, unit);

                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap
                        .get((compuMethodMap.get(c.getAxises().get(1).getRefCompuMethod()).getRefcomputab()));

                String[] stab = affectCOMEnum(byteperReadAxe2, nbpoints2, addressAxe2, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabaxisY.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                axisY.set(VALUE_FIELD, objtabaxisY);
                axisY.put(UNIT, "");
                break;
            default:
                break;
        }

        // Determine the Data
        String readway = recordlayoutMap.get(c.getRefRecordLayout()).isRowdir() ? ROW_DIR : COLUMN_DIR;
        switch (compuMethodMap.get(c.getRefComputMethod()).getType()) {
            case RAT_FUNC:
                String unit = compuMethodMap.get(c.getRefComputMethod()).getUnit().replaceAll("\"", "");

                double[] dtab = affectNumValues(recordlayoutMap.get(c.getRefRecordLayout()).getFncvalue(), addressData,
                        nbpoints1 * nbpoints2, byteperRead, compuMethodMap.get(c.getRefComputMethod()));
                for (int i = 0; i < dtab.length; i++) {
                    objtabdata.add(dtab[i]);
                }

                data.set(VALUE_FIELD, objtabdata);
                data.put(UNIT, unit);
                data.put(READWAY, readway);
                break;
            case TAB_VERB:
                CompuVTab enuObj = compuVTabMap.get((compuMethodMap.get(c.getRefComputMethod()).getRefcomputab()));
                String[] stab = affectCOMEnum(byteperRead, nbpoints1 * nbpoints2, addressData, enuObj);
                for (int i = 0; i < stab.length; i++) {
                    objtabdata.add((stab[i] == null || stab[i].isEmpty()) ? "" : stab[i].replaceAll("\"", ""));
                }
                data.set(VALUE_FIELD, objtabaxisY);
                data.put(UNIT, "");
                data.put(READWAY, readway);
                break;
            default:
                break;
        }

        c.setResult(main);
    }

    /**
     * Determine the set of values fetched from the HexFile with a start address
     * where the set of values begin, the number of values to fetch and finally the
     * number of byte to read for each value(with the dataType). Moreover a
     * computeMethod is applied to the values returned.
     *
     * @param d            The DataType to know if the values are signed
     * @param startAddress start address where the set of values begin
     * @param nbpoints     number of values to fetch
     * @param byteperRead  number of byte to read for each hexadecimal value
     * @param comp         CompuMethod to apply
     * @return an array of values decoded from the IntelHex File
     */
    private double[] affectNumValues(DataType d, String startAddress, int nbpoints, int byteperRead, CompuMethod comp) {
        double[] dtab = new double[nbpoints];

        String hexAddrAbs = "";
        for (int i = 0; i < nbpoints; i++) {
            hexAddrAbs = "0x" + Long.toHexString(Long.decode(startAddress) + i * byteperRead);
            dtab[i] = comp.rat_func(decodeValue(d, addressMap, byteperRead, hexAddrAbs));
        }
        return dtab;
    }

    /**
     * Convert a non-signed hexadecimal value from the IntelHex File(useful for
     * enumeration of Strings)
     *
     * @param addressDataMap HashMap with the data from the IntelHex Files
     * @param byteperRead    number of byte to read for each hexadecimal value
     * @param address        start address where the set of values begin
     * @return a decoded non-signed value (so positive) in decimal
     */
    private double decodePosValue(Map<String, String> addressDataMap, int byteperRead, String address) {
        String hexaValue = searchValueHEX(addressDataMap, byteperRead, address);
        if (hexaValue.isEmpty() || hexaValue.equals("0x")) {
            return 0;
        }
        return Long.decode(hexaValue);
    }

    /**
     * Convert a hexadecimal value fetch from the IntelHex File in different ways
     * depending on the Datatype (Byte signed or not, Integer Signed or Not)
     *
     * @param d              DataType of the value fetched (to know if the value is signed or
     *                       not)
     * @param addressDataMap HashMap from ParseHex containing the data of the IntelHex File
     * @param byteperRead    number of byte to read for each hexadecimal value
     * @param address        start address where the set of values begin
     * @return a decoded value in decimal
     */
    private double decodeValue(DataType d, Map<String, String> addressDataMap, int byteperRead, String address) {
        String hexaValue = searchValueHEX(addressDataMap, byteperRead, address);
        if (hexaValue.isEmpty() || hexaValue.equals("0x")) {
            return 0;
        }
        try {
            String bin = Long.toString(Long.parseLong(hexaValue.substring(2), 16), 2);
            switch (d) {
                case SBYTE:
                    if (bin.startsWith("1") && bin.length() == 8) {
                        return (Long.parseLong(bin.replace('0', 'X').replace('1', '0').replace('X', '1'), 2) + 1) * -1;
                    } else {
                        return Long.decode(hexaValue);
                    }
                case SWORD:
                    if (bin.startsWith("1") && bin.length() == 16) {
                        return (Long.parseLong(bin.replace('0', 'X').replace('1', '0').replace('X', '1'), 2) + 1) * -1;
                    } else {
                        return Long.decode(hexaValue);
                    }
                case SLONG:
                    if (bin.startsWith("1") && (bin.length() == 32)) {
                        return (Long.parseLong(bin.replace('0', 'X').replace('1', '0').replace('X', '1'), 2) + 1) * -1;
                    } else {
                        return Long.decode(hexaValue);
                    }
                case FLOAT32_IEEE:
                    if (bin.length() > 16) {
                        if (bin.length() < 32) {
                            int diff = 32 - bin.length();
                            for (int i = 0; i < diff; i++) {
                                bin = "0" + bin;
                            }
                        }
                        int s = (bin.startsWith("1")) ? (-1) : 1;
                        long e = Long.parseLong(bin.substring(1, 9), 2) - 127;
                        double m = 1;
                        for (int i = 1; i < bin.substring(9).length(); i++) {
                            int v = (bin.substring(9).charAt(i - 1) == '1') ? 1 : 0;
                            m += Math.pow(2, -i) * v;
                        }
                        return s * m * Math.pow(2, e);
                    } else {
                        return Long.decode(hexaValue);
                    }

                default:
                    return Long.decode(hexaValue);
            }
        } catch (Exception e) {
            logger.error(hexaValue + " " + e.toString() + " pour l'adresse suivante " + address + " et les datatypes "
                    + d);
            return 0;
        }

    }

    /**
     * Fetch a Hexadecimal value from the IntelHex File
     *
     * @param AddressDataMap HashMap from ParseHex containing the data of the IntelHex File
     * @param byteperRead    number of byte to read for each hexadecimal value
     * @param address        start address where the set of values begin
     * @return A String of the Hexadecimal value Fetch from the DATA in the IntelHex
     * File
     */
    private String searchValueHEX(Map<String, String> AddressDataMap, int byteperRead, String address) {
        Long decAddress = Long.decode(address);
        String decodedValueHEX = "0x";

        // When an address, near the address we're looking for, matches a key address
        // from the AddressDataMap
        for (long i = decAddress - dataRangeRecord; i < decAddress; i++) {
            if (AddressDataMap.containsKey(Long.toHexString(i))) {
                String invDecodedValueHex = "";
                // We take its position in the Data set of the AddressDataMap of the key address
                long diff = decAddress - i;
                try {
                    // We split the Dataset into an array of hexadecimal byte
                    String[] vData = AddressDataMap.get(Long.toHexString(i)).split(" ");
                    for (int j = 0; j < byteperRead; j++) {
                        try {
                            // Then we concatenate all the hexadecimal byte from the position within the
                            // byteperRead
                            invDecodedValueHex += vData[(int) (diff + j)] + " ";
                        } catch (IndexOutOfBoundsException e) {
                            // If the remaining byte(s) to read are in the next Dataset
                            // then we determine the number of remaining bytes and the next key address
                            // and concatenate the remaining byte(s) to read to the inverse hexadecimal
                            // value
                            int more = byteperRead - j;
                            j = byteperRead;
                            long nextplage = i + vData.length;
                            for (int k = 0; k < more; k++) {
                                invDecodedValueHex += AddressDataMap.get(Long.toHexString(nextplage)).split(" ")[k]
                                        + " ";
                            }
                        }
                    }

                    // The value we got here is in the wrong Significant Byte order so we inverse
                    // the byte to have the right value
                    if (!invDecodedValueHex.isEmpty()) {
                        String[] tab = invDecodedValueHex.split(" ");
                        for (int j = tab.length - 1; j >= 0; j--) {
                            decodedValueHEX += tab[j].substring(2);
                        }
                    }

                } catch (NullPointerException e) {
                    logger.error(e.getMessage() + " = pas de correspondance pour l'adresse : " + address
                            + " avec pour plage d'adresse " + Long.toHexString(i));
                }
                break;
            }
        }

        return decodedValueHEX;

    }

}
