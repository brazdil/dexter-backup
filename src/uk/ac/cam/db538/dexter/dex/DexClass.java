package uk.ac.cam.db538.dexter.dex;

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib.ClassDefItem;

import lombok.Getter;
import lombok.val;

public class DexClass {

	@Getter private final Dex ParentFile;
	
	@Getter	private final String FullName;
	@Getter	private final String PrettyName;
	@Getter	private final String ShortName;
	@Getter	private final String PackageName;
	
	@Getter private final Set<DexField> Fields;
	
	public DexClass(Dex parent, String fullname, Set<DexField> fields) {
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
		
		Fields = (fields == null) ? new HashSet<DexField>() : fields;
	}
	
	public DexClass(Dex parent, ClassDefItem clsInfo) {
		this(parent, 
		     clsInfo.getClassType().getTypeDescriptor(), 
		     null);
		
		val clsData = clsInfo.getClassData();
		if (clsData != null) {
			for (val staticFieldInfo : clsData.getStaticFields())
				Fields.add(new DexField(this, staticFieldInfo));
			for (val instanceFieldInfo : clsData.getInstanceFields())
				Fields.add(new DexField(this, instanceFieldInfo));
		}
	}
	
	public void addField(DexField f) {
		if (f.getParentClass() != null)
			f.getParentClass().removeField(f);
		
		Fields.add(f);
	}
	
	public void removeField(DexField f) {
		if (f.getParentClass() == this) {
			Fields.remove(f);
			f.setParentClass(null);
		}
	}
}
