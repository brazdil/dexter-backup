package uk.ac.cam.db538.dexter.dex.type;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexDouble;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexFloat;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexLong;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitive.DexShort;

public class DexTypeTest {

	@Test
	public void testIncorrectType() {
		val type = DexType.parse("X", null);
		assertNull(type);
	}

	@Test
	public void testVoid() {
		val type = DexType.parse("V", null);
		assertTrue(type instanceof DexVoid);
	}

	@Test
	public void testByte() {
		val type = DexType.parse("B", null);
		assertTrue(type instanceof DexByte);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testBoolean() {
		val type = DexType.parse("Z", null);
		assertTrue(type instanceof DexBoolean);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testShort() {
		val type = DexType.parse("S", null);
		assertTrue(type instanceof DexShort);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testChar() {
		val type = DexType.parse("C", null);
		assertTrue(type instanceof DexChar);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testInteger() {
		val type = DexType.parse("I", null);
		assertTrue(type instanceof DexInteger);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testLong() {
		val type = DexType.parse("J", null);
		assertTrue(type instanceof DexLong);
		assertEquals(2, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testFloat() {
		val type = DexType.parse("F", null);
		assertTrue(type instanceof DexFloat);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testDouble() {
		val type = DexType.parse("D", null);
		assertTrue(type instanceof DexDouble);
		assertEquals(2, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testClassType() {
		val type = DexType.parse("Ljava.lang.String;", null);
		assertTrue(type instanceof DexClassType);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
		
		val classType = (DexClassType) type;
		assertEquals("java.lang.String", classType.getPrettyName());
		assertEquals("java.lang", classType.getPackageName());
		assertEquals("String", classType.getShortName());
	}
	
	@Test
	public void testClassType_MissingSemicolon() {
		val type = DexType.parse("Ljava.lang.String", null);
		assertNull(type);
	}

	@Test
	public void testClassType_DefaultPackage() {
		val type = DexType.parse("LTestClass;", null);
		assertTrue(type instanceof DexClassType);
		
		val classType = (DexClassType) type;
		assertEquals("TestClass", classType.getPrettyName());
		assertNull(classType.getPackageName());
		assertEquals("TestClass", classType.getShortName());
	}

	@Test
	public void testClassType_NoCache() {
		val type1 = DexType.parse("Ljava.lang.String;", null);
		val type2 = DexType.parse("Ljava.lang.String;", null);
		assertFalse(type1 == type2);		
	}
	
	@Test
	public void testClassType_WithCache() {
		TypeCache cache = new TypeCache();
		val type1 = DexType.parse("Ljava.lang.String;", cache);
		val type2 = DexType.parse("Ljava.lang.String;", cache);
		assertTrue(type1 == type2);		
	}
	
	@Test
	public void testArrayType() {
		val type = DexType.parse("[I", null);
		assertTrue(type instanceof DexArrayType);
		assertTrue(((DexArrayType) type).getElementType() instanceof DexInteger);
		assertEquals(1, ((DexRegisterType) type).getRegisters());
	}

	@Test
	public void testArrayType_NoCache() {
		val type1 = DexType.parse("[I", null);
		val type2 = DexType.parse("[I", null);
		assertFalse(type1 == type2);
	}
	
	@Test
	public void testArrayType_WithCache() {
		TypeCache cache = new TypeCache();
		val type1 = DexType.parse("[I", cache);
		val type2 = DexType.parse("[I", cache);
		assertTrue(type1 == type2);
	}

	@Test
	public void testArrayType_UnknownElementType() {
		val type = DexType.parse("[X", null);
		assertNull(type);
	}
}
