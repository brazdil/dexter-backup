package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Pair;

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

  public static DexPrimitiveType parse(String typeDescriptor) {
    val primitive = PRIMITIVES_CACHE.get(typeDescriptor);
    if (primitive == null)
      throw new UnknownTypeException(typeDescriptor);
    return primitive;
  }

  public abstract Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache);

  public static class DexByte extends DexPrimitiveType {
    private DexByte() {
      super("B", "byte", DexRegisterTypeSize.SINGLE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Byte;", cache), "TYPE");
    }
  }

  public static class DexBoolean extends DexPrimitiveType {
    private DexBoolean() {
      super("Z", "boolean", DexRegisterTypeSize.SINGLE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Boolean;", cache), "TYPE");
    }
  }

  public static class DexShort extends DexPrimitiveType {
    private DexShort() {
      super("S", "short", DexRegisterTypeSize.SINGLE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Short;", cache), "TYPE");
    }
  }

  public static class DexChar extends DexPrimitiveType {
    private DexChar() {
      super("C", "char", DexRegisterTypeSize.SINGLE);
    }


    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Character;", cache), "TYPE");
    }
  }

  public static class DexInteger extends DexPrimitiveType {
    private DexInteger() {
      super("I", "int", DexRegisterTypeSize.SINGLE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Integer;", cache), "TYPE");
    }
  }

  public static class DexLong extends DexPrimitiveType {
    private DexLong() {
      super("J", "long", DexRegisterTypeSize.WIDE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Long;", cache), "TYPE");
    }
  }

  public static class DexFloat extends DexPrimitiveType {
    private DexFloat() {
      super("F", "float", DexRegisterTypeSize.SINGLE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Float;", cache), "TYPE");
    }
  }

  public static class DexDouble extends DexPrimitiveType {
    private DexDouble() {
      super("D", "double", DexRegisterTypeSize.WIDE);
    }

    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexParsingCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Double;", cache), "TYPE");
    }
  }
}
