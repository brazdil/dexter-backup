package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class DexType_Primitive extends DexType_Register {

  protected DexType_Primitive() { }
  
  public abstract Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache);

  public static DexType_Primitive parse(String typeDescriptor, DexTypeCache cache) {
	// try parsing the descriptor as a primitive of each given type
	  
	try {
	    return DexType_Boolean.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Byte.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Char.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Double.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Float.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Integer.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Long.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	try {
	    return DexType_Short.parse(typeDescriptor, cache);
	} catch (UnknownTypeException e) { }
	
	throw new UnknownTypeException(typeDescriptor);
  }
}
