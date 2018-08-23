package com.psagroup.calibrationparserapi.a2lobject.axis;

import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;

import lombok.Data;

@Data
public class AxisPts extends PrimaryObject {
	String address,description,label,maxdiff, inputquantity, format;
	double upper, lower ;
	int nbpoints;
	boolean absolute;
	String refComputMethod;
	String refRecordLayout;
}
