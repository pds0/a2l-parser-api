package com.psagroup.calibrationparserapi.a2lobject.axis;

import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;

import lombok.Data;

@Data
public class AxisDescr extends PrimaryObject {
	AxisType type;
	String address, inputquantity ;
	String refCompuMethod;
	int nbpoints;
	float lower, upper;
	
	String format, extended_limits, refAxisPts;
	
	double[] fixAxisPar;
	
}
