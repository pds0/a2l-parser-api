package com.psagroup.calibrationparserapi;

import com.psagroup.calibrationparserapi.a2lobject.characteristic.CharType;
import com.psagroup.calibrationparserapi.a2lobject.characteristic.Characteristic;
import com.psagroup.calibrationparserapi.parser.a2l.CharacteristicParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalibrationParserApiApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void characParsing() {
        CharacteristicParser characteristicParser = new CharacteristicParser();

        Characteristic characteristic = characteristicParser.buildA2lObject("/begin CHARACTERISTIC\n" +
                "\n" +
                "    EpmCaS_facPlausLow_GCUR\n" +
                "    \"factor for the lower limit of the plaus-check of the camshaft-signals\"\n" +
                "    CURVE\n" +
                "    0x806F4418\n" +
                "    CurG_Ws16\n" +
                "    65.53500\n" +
                "    EpmCaS_Fac_Plaus\n" +
                "    -32.76800\n" +
                "    32.76700\n" +
                "\n" +
                "    FORMAT \"%8.5\"\n" +
                "    EXTENDED_LIMITS -32.76800 32.76700\n" +
                "    \n" +
                "\n" +
                "    /begin AXIS_DESCR\n" +
                "\n" +
                "        COM_AXIS\n" +
                "        Epm_nEng\n" +
                "        EngN\n" +
                "        3\n" +
                "        -16384.00\n" +
                "        16383.50\n" +
                "\n" +
                "        FORMAT \"%8.2\"\n" +
                "        EXTENDED_LIMITS -16384.00 16383.50\n" +
                "\n" +
                "        AXIS_PTS_REF EpmCaS_facPlaus_DST\n" +
                "\n" +
                "    /end AXIS_DESCR\n" +
                "\n" +
                "/end CHARACTERISTIC");

        Assert.assertEquals("EpmCaS_facPlausLow_GCUR", characteristic.getLabel());
        Assert.assertEquals("factor for the lower limit of the plaus-check of the camshaft-signals", characteristic.getDescription());
        Assert.assertEquals(CharType.CURVE, characteristic.getChartype());
        Assert.assertEquals("0x806F4418", characteristic.getAddress());
        Assert.assertEquals("CurG_Ws16", characteristic.getRefRecordLayout());
        Assert.assertEquals("65.53500", characteristic.getMaxdiff());
        Assert.assertEquals("EpmCaS_Fac_Plaus", characteristic.getRefComputMethod());
        Assert.assertEquals(-32.76800, characteristic.getLower(), 0.001);
        Assert.assertEquals(32.76700, characteristic.getUpper(), 0.001);


        Characteristic characteristic2 = characteristicParser.buildA2lObject("/begin CHARACTERISTIC\n" +
                "\n" +
                "    InjVlv_numBnkTDC_CA\n" +
                "    \"Injection bank in which the cylinder is located (=x-1 for MVxy in the \"\"Brunnerplan\"\")\"\n" +
                "    VAL_BLK\n" +
                "    0x806F9238\n" +
                "    ValA_Wu8\n" +
                "    4.000\n" +
                "    OneToOne\n" +
                "    0.00\n" +
                "    4.000\n" +
                "\n" +
                "    FORMAT \"%5.3\"\n" +
                "    EXTENDED_LIMITS 0.00 255.0\n" +
                "    \n" +
                "    NUMBER 4");

        // For Quotes issue
        Assert.assertEquals("Injection bank in which the cylinder is located (=x-1 for MVxy in the Brunnerplan)", characteristic2.getDescription());

    }

}
