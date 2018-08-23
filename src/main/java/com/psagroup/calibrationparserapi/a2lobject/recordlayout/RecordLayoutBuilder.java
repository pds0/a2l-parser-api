package com.psagroup.calibrationparserapi.a2lobject.recordlayout;

import com.psagroup.calibrationparserapi.a2lobject.Builder;


public class RecordLayoutBuilder implements Builder {
	RecordLayout record = new RecordLayout();

	@Override
	public RecordLayout build() {
		RecordLayout recordToReturn = record;
		this.record = new RecordLayout();
		return recordToReturn;
	}

	public void label(String label) {
		this.record.setLabel(label);
	}

	public void fncValue(String fncvalue) {
		if (fncvalue.split("\\s+").length > 2) {
				switch (fncvalue.split("\\s+")[2]) {
				case "UBYTE":
					this.record.setFncvalue(DataType.UBYTE);

					break;
				case "SBYTE":
					this.record.setFncvalue(DataType.SBYTE);

					break;
				case "UWORD":
					this.record.setFncvalue(DataType.UWORD);

					break;
				case "SWORD":
					this.record.setFncvalue(DataType.SWORD);

					break;
				case "ULONG":
					this.record.setFncvalue(DataType.ULONG);

					break;
				case "SLONG":
					this.record.setFncvalue(DataType.SLONG);

					break;
				case "FLOAT32_IEEE":
					this.record.setFncvalue(DataType.FLOAT32_IEEE);

					break;
				default:
					break;
				}
				
				if(fncvalue.split("\\s+")[3].equals("ROW_DIR")) {
					this.record.setRowdir(true);
				}else {
					this.record.setRowdir(false);
				}
			}
		
	}
	
	public void axisPtsX(String axisptsx) {
		if (axisptsx.split("\\s+").length > 2) {
				switch (axisptsx.split("\\s+")[2]) {
				case "UBYTE":
					this.record.setAxisptsx(DataType.UBYTE);

					break;
				case "SBYTE":
					this.record.setAxisptsx(DataType.SBYTE);

					break;
				case "UWORD":
					this.record.setAxisptsx(DataType.UWORD);

					break;
				case "SWORD":
					this.record.setAxisptsx(DataType.SWORD);

					break;
				case "ULONG":
					this.record.setAxisptsx(DataType.ULONG);

					break;
				case "SLONG":
					this.record.setAxisptsx(DataType.SLONG);

					break;
				case "FLOAT32_IEEE":
					this.record.setAxisptsx(DataType.FLOAT32_IEEE);

					break;
				default:
					break;
				}
		
		}
	}
	
	
	public void axisPtsY(String axisptsy) {
		if (axisptsy.split("\\s+").length > 2) {
				switch (axisptsy.split("\\s+")[2]) {
				case "UBYTE":
					this.record.setAxisptsy(DataType.UBYTE);

					break;
				case "SBYTE":
					this.record.setAxisptsy(DataType.SBYTE);

					break;
				case "UWORD":
					this.record.setAxisptsy(DataType.UWORD);

					break;
				case "SWORD":
					this.record.setAxisptsy(DataType.SWORD);

					break;
				case "ULONG":
					this.record.setAxisptsy(DataType.ULONG);

					break;
				case "SLONG":
					this.record.setAxisptsy(DataType.SLONG);

					break;
				case "FLOAT32_IEEE":
					this.record.setAxisptsy(DataType.FLOAT32_IEEE);

					break;
				default:
					break;
				}
			}
		
	}
	
	public void noAxisPtsX(String noaxisptsx) {
		if (noaxisptsx.split("\\s+").length > 2) {
				switch (noaxisptsx.split("\\s+")[2]) {
				case "UBYTE":
					this.record.setNoaxisptsx(DataType.UBYTE);

					break;
				case "SBYTE":
					this.record.setNoaxisptsx(DataType.SBYTE);

					break;
				case "UWORD":
					this.record.setNoaxisptsx(DataType.UWORD);

					break;
				case "SWORD":
					this.record.setNoaxisptsx(DataType.SWORD);

					break;
				case "ULONG":
					this.record.setNoaxisptsx(DataType.ULONG);

					break;
				case "SLONG":
					this.record.setNoaxisptsx(DataType.SLONG);

					break;
				case "FLOAT32_IEEE":
					this.record.setNoaxisptsx(DataType.FLOAT32_IEEE);

					break;
				default:
					break;
				}
		
		}
	}
	
	public void noAxisPtsY(String noaxisptsy) {
		if (noaxisptsy.split("\\s+").length > 2) {
				switch (noaxisptsy.split("\\s+")[2]) {
				case "UBYTE":
					this.record.setNoaxisptsy(DataType.UBYTE);

					break;
				case "SBYTE":
					this.record.setNoaxisptsy(DataType.SBYTE);

					break;
				case "UWORD":
					this.record.setNoaxisptsy(DataType.UWORD);

					break;
				case "SWORD":
					this.record.setNoaxisptsy(DataType.SWORD);

					break;
				case "ULONG":
					this.record.setNoaxisptsy(DataType.ULONG);

					break;
				case "SLONG":
					this.record.setNoaxisptsy(DataType.SLONG);

					break;
				case "FLOAT32_IEEE":
					this.record.setNoaxisptsy(DataType.FLOAT32_IEEE);

					break;
				default:
					break;
				}
			}
		
	}

	/**
	 * @return the record
	 */
	public RecordLayout getRecord() {
		return record;
	}

}
