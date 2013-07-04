package uk.ac.cam.db538.dexter.dex.type;

import lombok.val;

public class DexType_Class extends DexType_Reference {

	private final String descriptor;
  
	private DexType_Class(String descriptor) {
		this.descriptor = descriptor;
	}
  
	public static DexType_Class parse(String typeDescriptor, DexTypeCache cache) {
		if (!typeDescriptor.startsWith("L") || !typeDescriptor.endsWith(";"))
			throw new UnknownTypeException(typeDescriptor);
		
		DexType_Class type = cache.getCachedType_Class(typeDescriptor);
		if (type == null) {
			type = new DexType_Class(typeDescriptor);
			cache.putCachedType_Class(typeDescriptor, type);
		}
		
		return type;
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}
	
	@Override
	public String getPrettyName() {
		return descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
	}
	
	public String getShortName() {
		val prettyName = getPrettyName();
		int lastDot = prettyName.lastIndexOf('.');
		if (lastDot == -1)
			return prettyName;
		else
			return prettyName.substring(lastDot + 1);
	}

	public String getPackageName() {
		val prettyName = getPrettyName();
		int lastDot = prettyName.lastIndexOf('.');
		if (lastDot == -1)
			return null;
		else
			return prettyName.substring(0, lastDot);
	}
}
