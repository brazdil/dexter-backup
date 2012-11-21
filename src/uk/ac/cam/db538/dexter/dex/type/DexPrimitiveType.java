package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public abstract class DexPrimitiveType extends DexRegisterType {

  public DexPrimitiveType(String descriptor, String prettyName, DexRegisterTypeSize typeSize) {
    super(descriptor, prettyName, typeSize);
  }

  /*
   * Create cache of primitive types and later
   * always return the same instance.
   */
  private static final Map<String, DexPrimitiveType> PrimitivesCache;
  private static void addToPrimitivesCache(DexPrimitiveType instance) {
    PrimitivesCache.put(instance.getDescriptor(), instance);
  }
  static {
    PrimitivesCache = new HashMap<String, DexPrimitiveType>();
    addToPrimitivesCache(new DexByte());
    addToPrimitivesCache(new DexBoolean());
    addToPrimitivesCache(new DexShort());
    addToPrimitivesCache(new DexChar());
    addToPrimitivesCache(new DexInteger());
    addToPrimitivesCache(new DexLong());
    addToPrimitivesCache(new DexFloat());
    addToPrimitivesCache(new DexDouble());
  }

  public static DexPrimitiveType parse(String typeDescriptor) throws UnknownTypeException {
    val primitive = PrimitivesCache.get(typeDescriptor);
    if (primitive == null)
      throw new UnknownTypeException(typeDescriptor);
    return primitive;
  }

  public static class DexByte extends DexPrimitiveType {
    public DexByte() {
      super("B", "byte", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexBoolean extends DexPrimitiveType {
    public DexBoolean() {
      super("Z", "boolean", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexShort extends DexPrimitiveType {
    public DexShort() {
      super("S", "short", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexChar extends DexPrimitiveType {
    public DexChar() {
      super("C", "char", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexInteger extends DexPrimitiveType {
    public DexInteger() {
      super("I", "int", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexLong extends DexPrimitiveType {
    public DexLong() {
      super("J", "long", DexRegisterTypeSize.WIDE);
    }
  }

  public static class DexFloat extends DexPrimitiveType {
    public DexFloat() {
      super("F", "float", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexDouble extends DexPrimitiveType {
    public DexDouble() {
      super("D", "double", DexRegisterTypeSize.WIDE);
    }
  }
}
