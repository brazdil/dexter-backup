package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class DexPrimitiveType extends DexRegisterType {

	private static final long serialVersionUID = 1L;
	
	protected DexPrimitiveType() { }
  
	public abstract Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache);

	public static DexPrimitiveType parse(String typeDescriptor, DexTypeCache cache) {
		// try parsing the descriptor as a primitive of each given type
		  
		try {
		    return DexBoolean.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexByte.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexChar.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexDouble.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexFloat.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexInteger.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexLong.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		try {
		    return DexShort.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }
		
		throw new UnknownTypeException(typeDescriptor);
	}
}
