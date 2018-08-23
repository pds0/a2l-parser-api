package com.psagroup.calibrationparserapi.a2lobject.recordlayout;

public enum DataType {
	UBYTE("unsigned 8 Bit",8),
	SBYTE("signed 8 Bit",8),
	UWORD("unsigned integer 16 Bit",16),
	SWORD("signed integer 16 Bit",16),
	ULONG("unsigned integer 32 Bit",32),
	SLONG("signed integer 32 Bit",32),
	FLOAT32_IEEE("float 32 Bit",32);

	private int nbbits;

	//Constructeur
	 DataType(String name, int nbbits){
		 this.setNbbits(nbbits);
	
	  }

	public int getNbbits() {
		return nbbits;
	}

	public void setNbbits(int nbbits) {
		this.nbbits = nbbits;
	}
	
	
}
