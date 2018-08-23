package com.psagroup.calibrationparserapi.parser.ulp;


public class SRecord {
    public String line;
    private Integer type;
    private Integer dataSize;
    private String address;
    private String data;
    private String checksum;

    public SRecord(String line) throws SRecordParseException {
        this.line = line;
        this.type = Character.getNumericValue(line.charAt(1));
        switch (type) {
            case 0:
            case 1:
            case 5:
            case 9:
                this.address = line.substring(4, 8);
                break;
            case 2:
            case 8:
                this.address = line.substring(4, 10);
                break;
            case 3:
            case 7:
                this.address = line.substring(4, 12);
                break;
            default:
                throw new SRecordParseException(line);
        }
        this.dataSize = Integer.parseInt(line.substring(2, 4), 16) - address.length() / 2 - 1;
        int beginDataIndex = address.length() + 4;

        int endDataIndex = beginDataIndex + dataSize * 2;
        if (type < 5) {
            try {
                this.data = line.substring(beginDataIndex, endDataIndex);
            } catch (StringIndexOutOfBoundsException e) {
                throw new SRecordParseException(line);
            }

        }
        this.checksum = line.substring(endDataIndex + 1);
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "SRecord{" +
                "line='" + line + '\n' +
                ", type=" + type +
                ", dataSize=" + dataSize +
                ", address='" + address + '\'' +
                ", data='" + data + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
