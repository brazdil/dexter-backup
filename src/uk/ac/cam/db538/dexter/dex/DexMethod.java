package uk.ac.cam.db538.dexter.dex;

import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import lombok.Getter;
import lombok.Setter;

public class DexMethod {
	
	@Getter @Setter private DexClass ParentClass;
	@Getter private final String Name;
	@Getter private final Set<AccessFlags> AccessFlagSet; 

	public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags) {
		ParentClass = parent;
		Name = name;
		AccessFlagSet = Utils.getNonNullAccessFlagSet(accessFlags);
	}
	
	public DexMethod(DexClass parent, EncodedMethod methodInfo) {
		this(parent,
		     methodInfo.method.getMethodName().getStringValue(),
		     Utils.getAccessFlagSet(methodInfo.accessFlags));
		
//		System.out.println(ParentClass.getPrettyName() + "." + Name);
//		System.out.println(methodInfo.method.getMethodString());
//		System.out.println(methodInfo.method.getShortMethodString());
//		
//		val prototype = methodInfo.method.getPrototype();
//		val params = prototype.getParameters();
//		if (params != null)
//			for (val type : params.getTypes())
//				System.out.println(type.getTypeDescriptor());
//		val returnType = prototype.getReturnType();
//		returnType.
	}
}
