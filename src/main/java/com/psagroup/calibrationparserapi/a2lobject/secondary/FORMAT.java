package com.psagroup.calibrationparserapi.a2lobject.secondary;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FORMAT extends SecondaryObject {
		String wholeformat;
		int unite;
		int decimal;
		
		public void setformat() {
			String[] f = wholeformat.substring(1).split(".");
			this.unite =Integer.parseInt(f[0]);
			this.decimal =Integer.parseInt(f[1]);			
		}

}
