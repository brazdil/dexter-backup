package uk.ac.cam.db538.dexter.dex;

import org.jf.dexlib.ClassDefItem;

import lombok.Getter;

public class DexClass {

	@Getter	private final String FullName;
	@Getter	private final String PrettyName;
	@Getter	private final String ShortName;
	@Getter	private final String PackageName;
	
	public DexClass(ClassDefItem cls) {
		FullName = cls.getClassType().getTypeDescriptor();
		PrettyName = FullName.substring(1, FullName.length() - 1).replace('/', '.');
		
		int lastDot = PrettyName.lastIndexOf('.');
		if (lastDot == -1) {
			ShortName = PrettyName;
			PackageName = null;
		} else {
			ShortName = PrettyName.substring(lastDot + 1);
			PackageName = PrettyName.substring(0, lastDot);
		}
	}

	@Override
	public String toString() {
		return this.getPrettyName();
	}
}
