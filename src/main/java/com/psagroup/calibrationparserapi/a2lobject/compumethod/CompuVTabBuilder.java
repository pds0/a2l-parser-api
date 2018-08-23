package com.psagroup.calibrationparserapi.a2lobject.compumethod;

import com.psagroup.calibrationparserapi.a2lobject.Builder;

import java.io.BufferedReader;
import java.io.IOException;

public class CompuVTabBuilder implements Builder {
    private CompuVTab comp = new CompuVTab();

    @Override
    public CompuVTab build() {
        CompuVTab compToReturn = comp;
        this.comp = new CompuVTab();
        return compToReturn;
    }

    public void label(String label) {
        this.comp.setLabel(label);
    }

    public void description(String description) {
        this.comp.setDescription(description);
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

    public void nbofStrings(String nbofStrings) {
        this.comp.setNbofStrings(Integer.parseInt(nbofStrings));
    }

    public void enu(BufferedReader reader) throws IOException {
        String line = reader.readLine().trim();
        while (line.split("\\s+")[0].matches("\\d+")) {
            this.comp.getEnu().put(Double.parseDouble(line.split("\\s+")[0]), line.substring(line.split("\\s+")[0].length() + 1));
            line = reader.readLine().trim();
        }
    }

    public void enuLR(String[] pair) throws IOException {
        this.comp.getEnu().put(Double.parseDouble(pair[0]), pair[1]);
    }


    /**
     * @return the comp
     */
    public CompuVTab getComp() {
        return comp;
    }


}
