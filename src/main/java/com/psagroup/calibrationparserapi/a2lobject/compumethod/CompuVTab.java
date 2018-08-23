package com.psagroup.calibrationparserapi.a2lobject.compumethod;

import java.util.*;

import com.psagroup.calibrationparserapi.a2lobject.PrimaryObject;

import lombok.Data;

@Data
public class CompuVTab extends PrimaryObject {
	private String label;
	private String description;
	private ConversionType type;
	int nbofStrings;
	HashMap<Double, String> enu =  new HashMap<>();
}
