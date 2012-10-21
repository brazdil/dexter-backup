package uk.ac.cam.db538.dexter.dex;

import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.ClassDefItem;

import lombok.Getter;
import lombok.val;

public class DexClass {

	@Getter private final Dex ParentFile;
	
	@Getter	private final String FullName;
	@Getter	private final String PrettyName;
	@Getter	private final String ShortName;
	@Getter	private final String PackageName;
	
	@Getter private final List<DexField> StaticFields;
	@Getter private final List<DexField> InstanceFields;
	
	public DexClass(Dex parent, String fullname, List<DexField> staticFields, List<DexField> instanceFields) {
		assert(fullname.startsWith("L"));
		assert(fullname.endsWith(";"));
		
		ParentFile = parent;
		
		FullName = fullname;
		PrettyName = FullName.substring(1, FullName.length() - 1).replace('/', '.');
		
		int lastDot = PrettyName.lastIndexOf('.');
		if (lastDot == -1) {
			ShortName = PrettyName;
			PackageName = null;
		} else {
			ShortName = PrettyName.substring(lastDot + 1);
			PackageName = PrettyName.substring(0, lastDot);
		}
		
		StaticFields = (staticFields == null) ? new LinkedList<DexField>() : staticFields;
		InstanceFields = (instanceFields == null) ? new LinkedList<DexField>() : instanceFields;
	}
	
	public DexClass(Dex parent, ClassDefItem clsInfo) {
		this(parent, 
		     clsInfo.getClassType().getTypeDescriptor(), 
		     null,
		     null);
		
		val clsData = clsInfo.getClassData();
		if (clsData != null) {
			for (val staticFieldInfo : clsData.getStaticFields())
				StaticFields.add(new DexField(this, staticFieldInfo));
			for (val instanceFieldInfo : clsData.getInstanceFields())
				InstanceFields.add(new DexField(this, instanceFieldInfo));
		}
	}
	
	@Override
	public String toString() {
		return this.getPrettyName();
	}
}
