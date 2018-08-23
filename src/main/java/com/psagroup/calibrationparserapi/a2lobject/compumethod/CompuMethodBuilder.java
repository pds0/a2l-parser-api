package com.psagroup.calibrationparserapi.a2lobject.compumethod;

import com.psagroup.calibrationparserapi.a2lobject.Builder;


public class CompuMethodBuilder implements Builder {
    private CompuMethod comp = new CompuMethod();

    @Override
    public CompuMethod build() {
        CompuMethod compToReturn = comp;
        this.comp = new CompuMethod();
        return compToReturn;
    }

    public void label(String label) {
        this.comp.setLabel(label);
    }

    public void description(String description) {
        this.comp.setDescription(description);
    }

    public void format(String format) {
        this.comp.setFormat(format);
    }

    public void unit(String unit) {
        this.comp.setUnit(unit);
    }

    public void type(String functiontype) {

        switch (functiontype) {
            case "IDENTICAL":
                this.comp.setType(ConversionType.IDENTICAL);
                break;
            case "LINEAR":
                this.comp.setType(ConversionType.LINEAR);
                break;
            case "RAT_FUNC":
                this.comp.setType(ConversionType.RAT_FUNC);
                break;
            case "TAB_INTP":
                this.comp.setType(ConversionType.TAB_INTP);
                break;
            case "TAB_NOINTP":
                this.comp.setType(ConversionType.TAB_NOINTP);
                break;
            case "TAB_VERB":
                this.comp.setType(ConversionType.TAB_VERB);
                break;
            case "FORM":
                this.comp.setType(ConversionType.FORM);
                break;

            default:
                break;
        }
    }

    public void coeffs(String coeffs) {
        String[] coeffArray = coeffs.split(" ");
        if (coeffArray.length > 1) {
            if (coeffArray[0].trim().equals("COEFFS")) {
                double tab[] = new double[6];
                for (int i = 0; i < coeffArray.length - 1; i++) {
                    tab[i] = Double.parseDouble(coeffArray[i + 1]);
                }
                this.comp.setCoeffs(tab);
            } else {
                System.err.printf("Problème de coherence entre le type RATFUNC de la compMethod %s et le mot clé %s", comp.getLabel(), coeffArray[0]);
            }
        }
    }

    public void refcomputab(String comptab) {
        String[] split = comptab.split(" ");
        if (split.length > 1) {
            if (split[0].trim().equals("COMPU_TAB_REF")) {
                this.comp.setRefcomputab(split[1].trim());
            } else {
                System.err.printf("Problème de coherence entre le type COMPU_TAB_REF de la compMethod %s et le mot clé %s", comp.getLabel(), split[0]);
            }
        }
    }

    /**
     * @return the comp
     */
    public CompuMethod getComp() {
        return comp;
    }

    /**
     * @param comp the comp to set
     */
    public void setComp(CompuMethod comp) {
        this.comp = comp;
    }


}
