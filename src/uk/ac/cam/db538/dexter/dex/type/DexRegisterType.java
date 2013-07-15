package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;

public abstract class DexRegisterType extends DexType {

	private static final long serialVersionUID = 1L;

	protected DexRegisterType() { }

	public static DexRegisterType parse(String typeDescriptor, DexTypeCache cache) throws UnknownTypeException {
		try {
			return DexPrimitiveType.parse(typeDescriptor, cache);
		} catch (UnknownTypeException e) { }

		return DexReferenceType.parse(typeDescriptor, cache);
	}
  
	public static String jvm2dalvik(String jvmName) throws UnknownTypeException {
		try {
			return DexPrimitiveType.jvm2dalvik(jvmName);
		} catch (UnknownTypeException e) { }

		return DexReferenceType.jvm2dalvik(jvmName);
	}

	public abstract RegisterWidth getTypeSize();

	public boolean isWide() {
		return getTypeSize() == RegisterWidth.WIDE;
	}

	public int getRegisters() {
		return getTypeSize().getRegisterCount();
	}
}
