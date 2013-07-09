package uk.ac.cam.db538.dexter.dex.type;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.Test;


public class DexType_Test {

  @Test(expected=UnknownTypeException.class)
  public void testIncorrectType() throws UnknownTypeException {
    DexType.parse("X", new DexTypeCache());
  }

  @Test
  public void testVoid() throws UnknownTypeException {
    val type = DexType.parse("V", new DexTypeCache());
    assertTrue(type instanceof DexVoid);
  }

  @Test
  public void testByte() throws UnknownTypeException {
    val type = DexType.parse("B", new DexTypeCache());
    assertTrue(type instanceof DexByte);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testBoolean() throws UnknownTypeException {
    val type = DexType.parse("Z", new DexTypeCache());
    assertTrue(type instanceof DexBoolean);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testShort() throws UnknownTypeException {
    val type = DexType.parse("S", new DexTypeCache());
    assertTrue(type instanceof DexShort);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testChar() throws UnknownTypeException {
    val type = DexType.parse("C", new DexTypeCache());
    assertTrue(type instanceof DexChar);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testInteger() throws UnknownTypeException {
    val type = DexType.parse("I", new DexTypeCache());
    assertTrue(type instanceof DexInteger);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testLong() throws UnknownTypeException {
    val type = DexType.parse("J", new DexTypeCache());
    assertTrue(type instanceof DexLong);
    assertEquals(2, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testFloat() throws UnknownTypeException {
    val type = DexType.parse("F", new DexTypeCache());
    assertTrue(type instanceof DexFloat);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testDouble() throws UnknownTypeException {
    val type = DexType.parse("D", new DexTypeCache());
    assertTrue(type instanceof DexDouble);
    assertEquals(2, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testClassType() throws UnknownTypeException {
    val type = DexType.parse("Ljava.lang.String;", new DexTypeCache());
    assertTrue(type instanceof DexClassType);
    assertEquals(1, ((DexRegisterType) type).getRegisters());

    val classType = (DexClassType) type;
    assertEquals("java.lang.String", classType.getPrettyName());
    assertEquals("java.lang", classType.getPackageName());
    assertEquals("String", classType.getShortName());
  }

  @Test(expected=UnknownTypeException.class)
  public void testClassType_MissingSemicolon() throws UnknownTypeException {
    DexType.parse("Ljava.lang.String", new DexTypeCache());
  }

  @Test
  public void testClassType_DefaultPackage() throws UnknownTypeException {
    val type = DexType.parse("LTestClass;", new DexTypeCache());
    assertTrue(type instanceof DexClassType);

    val classType = (DexClassType) type;
    assertEquals("TestClass", classType.getPrettyName());
    assertNull(classType.getPackageName());
    assertEquals("TestClass", classType.getShortName());
  }

  @Test
  public void testClassType_Cache() throws UnknownTypeException {
    DexTypeCache cache = new DexTypeCache();
    val type1 = DexType.parse("Ljava.lang.String;", cache);
    val type2 = DexType.parse("Ljava.lang.String;", cache);
    assertTrue(type1 == type2);
  }

  @Test
  public void testArrayType() throws UnknownTypeException {
    val type = DexType.parse("[I", new DexTypeCache());
    assertTrue(type instanceof DexArrayType);
    assertTrue(((DexArrayType) type).getElementType() instanceof DexInteger);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testArrayType_Cache() throws UnknownTypeException {
    DexTypeCache cache = new DexTypeCache();
    val type1 = DexType.parse("[I", cache);
    val type2 = DexType.parse("[I", cache);
    assertTrue(type1 == type2);
  }

  @Test(expected=UnknownTypeException.class)
  public void testArrayType_UnknownElementType() throws UnknownTypeException {
    DexType.parse("[X", new DexTypeCache());
  }
}
