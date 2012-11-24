package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public abstract class DexPrimitiveType extends DexRegisterType {

  protected DexPrimitiveType(String descriptor, String prettyName, DexRegisterTypeSize typeSize) {
    super(descriptor, prettyName, typeSize);
  }

  /*
   * Create cache of primitive types and later
   * always return the same instance.
   */
  private static final Map<String, DexPrimitiveType> PRIMITIVES_CACHE;
  private static void addToPrimitivesCache(DexPrimitiveType instance) {
    PRIMITIVES_CACHE.put(instance.getDescriptor(), instance);
  }
  static {
    PRIMITIVES_CACHE = new HashMap<String, DexPrimitiveType>();
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
    val primitive = PRIMITIVES_CACHE.get(typeDescriptor);
    if (primitive == null)
      throw new UnknownTypeException(typeDescriptor);
    return primitive;
  }

  public static class DexByte extends DexPrimitiveType {
    private DexByte() {
      super("B", "byte", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexBoolean extends DexPrimitiveType {
    private DexBoolean() {
      super("Z", "boolean", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexShort extends DexPrimitiveType {
    private DexShort() {
      super("S", "short", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexChar extends DexPrimitiveType {
    private DexChar() {
      super("C", "char", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexInteger extends DexPrimitiveType {
    private DexInteger() {
      super("I", "int", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexLong extends DexPrimitiveType {
    private DexLong() {
      super("J", "long", DexRegisterTypeSize.WIDE);
    }
  }

  public static class DexFloat extends DexPrimitiveType {
    private DexFloat() {
      super("F", "float", DexRegisterTypeSize.SINGLE);
    }
  }

  public static class DexDouble extends DexPrimitiveType {
    private DexDouble() {
      super("D", "double", DexRegisterTypeSize.WIDE);
    }
  }
}
