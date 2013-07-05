package uk.ac.cam.db538.dexter.dex.type;

import java.util.Arrays;
import java.util.List;

public abstract class DexReferenceType extends DexRegisterType {

	private static final long serialVersionUID = 1L;
	
	public static final DexRegisterTypeSize TypeSize = DexRegisterTypeSize.SINGLE;
	
	protected DexReferenceType() { }

	public static DexReferenceType parse(String typeDescriptor, DexTypeCache cache) throws UnknownTypeException {
	    try {
	      return DexClassType.parse(typeDescriptor, cache);
	    } catch (UnknownTypeException e) { }
	
	    try {
	      return DexArrayType.parse(typeDescriptor, cache);
	    } catch (UnknownTypeException e) { }
	
	    throw new UnknownTypeException(typeDescriptor);
	}

	@Override
	public DexRegisterTypeSize getTypeSize() {
		return TypeSize;
	}
  
	private static List<String> sImmutables = Arrays.asList(
		"Ljava/lang/String;",
		"Ljava/lang/Boolean;",
		"Ljava/lang/Byte;",
		"Ljava/lang/Character;",
		"Ljava/lang/Double;",
		"Ljava/lang/Float;",
		"Ljava/lang/Integer;",
		"Ljava/lang/Long;",
		"Ljava/lang/Short;"
	);
  
	public boolean isImmutable() {
		return sImmutables.contains(this.getDescriptor());
	}
}
