package com.psagroup.calibrationparserapi.a2lobject.compumethod;

import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;
import lombok.Data;


@Data
public class CompuMethod extends PrimaryObject {
    private String label;
    private String description;
    private ConversionType type;
    private String format;
    private String unit;
    private String refcomputab;

    private double[] coeffs;


    public double rat_func(double x) {
        double top = 0;
        double bottom = 1;
        if (coeffs[4] == 0) {
            top = coeffs[5] * x - coeffs[2];
            bottom = coeffs[1];

        } else if (coeffs[4] != 0 && coeffs[5] == 0 && coeffs[1] == 0) {
            top = coeffs[2];
            bottom = coeffs[4] * x;
        }

        if (bottom == 0) {
            System.err.println("Rat Func pas adaptee pour les coeffs (denominatuer = 0) pour la COMP_METHOD " + label);
        }
        return top / bottom;
    }


}


