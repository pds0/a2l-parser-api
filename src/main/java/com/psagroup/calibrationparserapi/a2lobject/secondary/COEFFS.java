package com.psagroup.calibrationparserapi.a2lobject.secondary;

public class COEFFS extends SecondaryObject {
	float[] tab = new float[6];
	
	public void setCoeffs(String currentLine) {
		String [] stab = currentLine.trim().replace("//s+", " ").split(" ");
		for (int i = 1; i < stab.length; i++) {
			this.tab[i-1] =Float.parseFloat(stab[i]);
		}
		
	}

}
