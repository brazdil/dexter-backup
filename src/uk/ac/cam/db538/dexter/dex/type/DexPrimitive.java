package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

public abstract class DexPrimitive extends DexRegisterType {

	public DexPrimitive(String descriptor, String prettyName, int registers) {
		super(descriptor, prettyName, registers);
	}
	
	/*
	 * Create cache of primitive types and later
	 * always return the same instance.
	 */
	private static final Map<String, DexPrimitive> PrimitivesCache;
	private static void addToPrimitivesCache(DexPrimitive instance) {
		PrimitivesCache.put(instance.getDescriptor(), instance);
	}
	static {
		PrimitivesCache = new HashMap<String, DexPrimitive>();
		addToPrimitivesCache(new DexByte());
		addToPrimitivesCache(new DexBoolean());
		addToPrimitivesCache(new DexShort());
		addToPrimitivesCache(new DexChar());
		addToPrimitivesCache(new DexInteger());
		addToPrimitivesCache(new DexLong());
		addToPrimitivesCache(new DexFloat());
		addToPrimitivesCache(new DexDouble());
	}
	
	public static DexPrimitive parse(String descriptor) {
		return PrimitivesCache.get(descriptor);
	}
	
	public static class DexByte extends DexPrimitive {
		public DexByte() {
			super("B", "byte", 1);
		}
	}

	public static class DexBoolean extends DexPrimitive {
		public DexBoolean() {
			super("Z", "boolean", 1);
		}
	}

	public static class DexShort extends DexPrimitive {
		public DexShort() {
			super("S", "short", 1);
		}
	}

	public static class DexChar extends DexPrimitive {
		public DexChar() {
			super("C", "char", 1);
		}
	}

	public static class DexInteger extends DexPrimitive {
		public DexInteger() {
			super("I", "integer", 1);
		}
	}

	public static class DexLong extends DexPrimitive {
		public DexLong() {
			super("J", "long", 2);
		}
	}

	public static class DexFloat extends DexPrimitive {
		public DexFloat() {
			super("F", "float", 1);
		}
	}

	public static class DexDouble extends DexPrimitive {
		public DexDouble() {
			super("D", "double", 2);
		}
	}
}
