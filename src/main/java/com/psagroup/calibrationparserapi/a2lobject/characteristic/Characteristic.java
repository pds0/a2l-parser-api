package com.psagroup.calibrationparserapi.a2lobject.characteristic;


import java.util.LinkedList;

import com.fasterxml.jackson.databind.JsonNode;
import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;
import com.psagroup.calibrationparserapi.a2lobject.axis.AxisDescr;

import lombok.Data;


@Data
public class Characteristic extends PrimaryObject {
	String label,address,description, maxdiff, format, extended_limits;
	double upper, lower ;
	int number;
	CharType chartype;
	
	String refComputMethod;
	String refRecordLayout;
	
	LinkedList<AxisDescr> axises = new LinkedList<>();
	
	JsonNode result;
	
	

	
		

}
