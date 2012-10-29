package uk.ac.cam.db538.dexter.dex.type;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexDouble;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexFloat;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexLong;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexShort;

public class DexTypeTest {

  @Test(expected=UnknownTypeException.class)
  public void testIncorrectType() throws UnknownTypeException {
    DexType.parse("X", null);
  }

  @Test
  public void testVoid() throws UnknownTypeException {
    val type = DexType.parse("V", null);
    assertTrue(type instanceof DexVoid);
  }

  @Test
  public void testByte() throws UnknownTypeException {
    val type = DexType.parse("B", null);
    assertTrue(type instanceof DexByte);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testBoolean() throws UnknownTypeException {
    val type = DexType.parse("Z", null);
    assertTrue(type instanceof DexBoolean);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testShort() throws UnknownTypeException {
    val type = DexType.parse("S", null);
    assertTrue(type instanceof DexShort);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testChar() throws UnknownTypeException {
    val type = DexType.parse("C", null);
    assertTrue(type instanceof DexChar);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testInteger() throws UnknownTypeException {
    val type = DexType.parse("I", null);
    assertTrue(type instanceof DexInteger);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testLong() throws UnknownTypeException {
    val type = DexType.parse("J", null);
    assertTrue(type instanceof DexLong);
    assertEquals(2, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testFloat() throws UnknownTypeException {
    val type = DexType.parse("F", null);
    assertTrue(type instanceof DexFloat);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testDouble() throws UnknownTypeException {
    val type = DexType.parse("D", null);
    assertTrue(type instanceof DexDouble);
    assertEquals(2, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testClassType() throws UnknownTypeException {
    val type = DexType.parse("Ljava.lang.String;", null);
    assertTrue(type instanceof DexClassType);
    assertEquals(1, ((DexRegisterType) type).getRegisters());

    val classType = (DexClassType) type;
    assertEquals("java.lang.String", classType.getPrettyName());
    assertEquals("java.lang", classType.getPackageName());
    assertEquals("String", classType.getShortName());
  }

  @Test(expected=UnknownTypeException.class)
  public void testClassType_MissingSemicolon() throws UnknownTypeException {
    DexType.parse("Ljava.lang.String", null);
  }

  @Test
  public void testClassType_DefaultPackage() throws UnknownTypeException {
    val type = DexType.parse("LTestClass;", null);
    assertTrue(type instanceof DexClassType);

    val classType = (DexClassType) type;
    assertEquals("TestClass", classType.getPrettyName());
    assertNull(classType.getPackageName());
    assertEquals("TestClass", classType.getShortName());
  }

  @Test
  public void testClassType_NoCache() throws UnknownTypeException {
    val type1 = DexType.parse("Ljava.lang.String;", null);
    val type2 = DexType.parse("Ljava.lang.String;", null);
    assertFalse(type1 == type2);
  }

  @Test
  public void testClassType_WithCache() throws UnknownTypeException {
    DexParsingCache cache = new DexParsingCache();
    val type1 = DexType.parse("Ljava.lang.String;", cache);
    val type2 = DexType.parse("Ljava.lang.String;", cache);
    assertTrue(type1 == type2);
  }

  @Test
  public void testArrayType() throws UnknownTypeException {
    val type = DexType.parse("[I", null);
    assertTrue(type instanceof DexArrayType);
    assertTrue(((DexArrayType) type).getElementType() instanceof DexInteger);
    assertEquals(1, ((DexRegisterType) type).getRegisters());
  }

  @Test
  public void testArrayType_NoCache() throws UnknownTypeException {
    val type1 = DexType.parse("[I", null);
    val type2 = DexType.parse("[I", null);
    assertFalse(type1 == type2);
  }

  @Test
  public void testArrayType_WithCache() throws UnknownTypeException {
    DexParsingCache cache = new DexParsingCache();
    val type1 = DexType.parse("[I", cache);
    val type2 = DexType.parse("[I", cache);
    assertTrue(type1 == type2);
  }

  @Test(expected=UnknownTypeException.class)
  public void testArrayType_UnknownElementType() throws UnknownTypeException {
    DexType.parse("[X", null);
  }
}
