package com.psagroup.calibrationparserapi.a2lobject.recordlayout;

import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;
import lombok.Data;

@Data
public class RecordLayout extends PrimaryObject {
    String label;
    boolean rowdir;
    DataType fncvalue, axisptsx, axisptsy, noaxisptsx, noaxisptsy;
}
