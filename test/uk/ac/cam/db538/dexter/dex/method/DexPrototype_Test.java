package uk.ac.cam.db538.dexter.dex.method;

import static org.junit.Assert.*;

import java.util.Arrays;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexPrototype_Test {

  @Test
  public void testGetInstrumentedPrototype_ReturnValue_OneWordPrimitives() {
    val cache = new DexInstrumentationCache(new DexParsingCache());

    val typeBoolean = DexType.parse("Z", cache.getParsingCache());
    val typeChar = DexType.parse("C", cache.getParsingCache());
    val typeByte = DexType.parse("B", cache.getParsingCache());
    val typeShort = DexType.parse("S", cache.getParsingCache());
    val typeInteger = DexType.parse("I", cache.getParsingCache());
    val typeFloat = DexType.parse("F", cache.getParsingCache());

    assertEquals("J", new DexPrototype(typeBoolean, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("J", new DexPrototype(typeChar, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("J", new DexPrototype(typeByte, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("J", new DexPrototype(typeShort, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("J", new DexPrototype(typeInteger, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("J", new DexPrototype(typeFloat, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
  }

  @Test
  public void testGetInstrumentedPrototype_ReturnValue_TwoWordPrimitives() {
    val cache = new DexInstrumentationCache(new DexParsingCache());

    val typeLong = DexType.parse("J", cache.getParsingCache());
    val typeDouble = DexType.parse("D", cache.getParsingCache());

    assertEquals("Ljava/lang/Long;", new DexPrototype(typeLong, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
    assertEquals("Ljava/lang/Double;", new DexPrototype(typeDouble, null).getInstrumentedPrototype(cache).getReturnType().getDescriptor());
  }

  @Test
  public void testGetInstrumentedPrototype_ReturnValue_Objects() {
    val cache = new DexInstrumentationCache(new DexParsingCache());

    val typeObject = DexType.parse("Ljava/lang/Object;", cache.getParsingCache());
    val typeString = DexType.parse("Ljava/lang/String;", cache.getParsingCache());

    assertEquals(typeObject, new DexPrototype(typeObject, null).getInstrumentedPrototype(cache).getReturnType());
    assertEquals(typeString, new DexPrototype(typeString, null).getInstrumentedPrototype(cache).getReturnType());
  }

  @Test
  public void testGetInstrumentedPrototype_ReturnValue_Void() {
    val cache = new DexInstrumentationCache(new DexParsingCache());

    val typeVoid = DexType.parse("V", cache.getParsingCache());

    assertEquals(typeVoid, new DexPrototype(typeVoid, null).getInstrumentedPrototype(cache).getReturnType());
  }

  @Test
  public void testGetInstrumentedPrototype_Params() {
    val cache = new DexInstrumentationCache(new DexParsingCache());

    val typeVoid = DexType.parse("V", cache.getParsingCache());
    val typeBoolean = DexRegisterType.parse("Z", cache.getParsingCache());
    val typeChar = DexRegisterType.parse("C", cache.getParsingCache());
    val typeByte = DexRegisterType.parse("B", cache.getParsingCache());
    val typeShort = DexRegisterType.parse("S", cache.getParsingCache());
    val typeInteger = DexRegisterType.parse("I", cache.getParsingCache());
    val typeFloat = DexRegisterType.parse("F", cache.getParsingCache());
    val typeLong = DexRegisterType.parse("J", cache.getParsingCache());
    val typeDouble = DexRegisterType.parse("D", cache.getParsingCache());
    val typeObject = DexRegisterType.parse("Ljava/lang/Object;", cache.getParsingCache());
    val typeString = DexRegisterType.parse("Ljava/lang/String;", cache.getParsingCache());

    // check that it adds extra parameter for the primitive types, and doesn't for reference types

    assertEquals(Arrays.asList(new DexRegisterType[] { typeBoolean, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeBoolean })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeChar, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeChar })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeByte, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeByte })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeShort, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeShort })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeInteger, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeInteger })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeFloat, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeFloat })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeLong, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeLong })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeDouble, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeDouble })).getInstrumentedPrototype(cache).getParameterTypes());

    assertEquals(Arrays.asList(new DexRegisterType[] { typeObject }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeObject })).getInstrumentedPrototype(cache).getParameterTypes());
    assertEquals(Arrays.asList(new DexRegisterType[] { typeString }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeString })).getInstrumentedPrototype(cache).getParameterTypes());

    // check that it adds them at the end

    assertEquals(Arrays.asList(new DexRegisterType[] { typeByte, typeShort, typeObject, typeLong, typeInteger, typeInteger, typeInteger }),
                 new DexPrototype(typeVoid, Arrays.asList(new DexRegisterType[] { typeByte, typeShort, typeObject, typeLong })).getInstrumentedPrototype(cache).getParameterTypes());
  }
}
