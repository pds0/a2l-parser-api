package com.psagroup.calibrationparserapi.a2lobject.axis;

import com.psagroup.calibrationparserapi.a2lobject.Builder;

public class AxisPtsBuilder implements Builder {
    private AxisPts axis = new AxisPts();

    @Override
    public AxisPts build() {
        AxisPts axisToReturn = axis;
        this.axis = new AxisPts();
        return axisToReturn;
    }


    public void label(String label) {
        axis.setLabel(label);
    }

    public void description(String description) {
        axis.setDescription(description);
    }

    public void address(String address) {
        axis.setAddress(address);
    }

    public void maxdiff(String maxdiff) {
        axis.setMaxdiff(maxdiff);
    }

    public void inputquantity(String inputquantity) {
        axis.setInputquantity(inputquantity);
    }

    public void upper(String upper) {
        axis.setUpper(Double.parseDouble(upper));
    }

    public void lower(String lower) {
        axis.setLower(Double.parseDouble(lower));
    }

    public void nbpoints(String nbpoints) {
        axis.setNbpoints(Integer.parseInt(nbpoints));
    }

    public void refComputMethod(String refComputMethod) {
        axis.setRefComputMethod(refComputMethod);
    }

    public void refRecordLayout(String refRecordLayout) {
        axis.setRefRecordLayout(refRecordLayout);
    }


    public void format(String format) {
        if (format.split(" ").length > 1) {
            if (format.split(" ")[0].trim().equals("FORMAT")) {
                axis.setFormat(format.split(" ")[1]);
            }
        }
    }

    public void absolute(String absolute) {
        if (absolute.split(" ").length > 1) {
            if (absolute.split(" ")[0].trim().equals("DEPOSIT")) {
                if (absolute.split(" ")[1].trim().equals("ABSOLUTE")) {
                    axis.setAbsolute(true);
                } else {
                    axis.setAbsolute(false);
                }
            }
        }
    }


    /**
     * @return the axis
     */
    public AxisPts getAxis() {
        return axis;
    }

}
