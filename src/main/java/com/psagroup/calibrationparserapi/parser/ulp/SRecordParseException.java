package com.psagroup.calibrationparserapi.parser.ulp;

public class SRecordParseException extends Exception {
    private String line;
    private String message;

    public SRecordParseException(String line) {
        this.line = line;
        this.message="Parsing SRecord ERROR for line " + line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
