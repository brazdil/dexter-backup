package uk.ac.cam.db538.dexter.hierarchy;

import static org.junit.Assert.*;

import java.io.File;

import lombok.val;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class BaseClassDefinition_Test {

	private RuntimeHierarchy hierarchy;
	private DexTypeCache typeCache;
	
	private ClassDefinition classObject;
	private ClassDefinition classThrowable;
	private ClassDefinition classException;
	private ClassDefinition classError;
	private InterfaceDefinition classList;
	
	@Before
	public void setUp() throws Exception {
		hierarchy = HierarchyBuilder.deserialize(new File("test/hierarchy.dump")).build();
		typeCache = hierarchy.getTypeCache();
		
		val typeObject = DexClassType.parse("Ljava/lang/Object;", typeCache);
		classObject = hierarchy.getClassDefinition(typeObject);

		val typeThrowable = DexClassType.parse("Ljava/lang/Throwable;", typeCache);
		classThrowable = hierarchy.getClassDefinition(typeThrowable);

		val typeException = DexClassType.parse("Ljava/lang/Exception;", typeCache);
		classException = hierarchy.getClassDefinition(typeException);

		val typeError = DexClassType.parse("Ljava/lang/Error;", typeCache);
		classError = hierarchy.getClassDefinition(typeError);
		
		val typeList = DexClassType.parse("Ljava/util/List;", typeCache);
		classList = hierarchy.getInterfaceDefinition(typeList);
	}

	@Test
	public void test_IsRoot() {
		assertTrue(classObject.isRoot());
		assertFalse(classThrowable.isRoot());
		assertFalse(classException.isRoot());
		assertFalse(classList.isRoot());
	}

	@Test
	public void test_IsAbstract() {
		assertFalse(classException.isAbstract());
		assertTrue(classList.isAbstract());
	}
	
	@Test
	public void test_IsChildOf_Reflexivity() {
		assertTrue(classObject.isChildOf(classObject));
		assertTrue(classException.isChildOf(classException));
		assertTrue(classThrowable.isChildOf(classThrowable));
		assertTrue(classList.isChildOf(classList));
	}

	@Test
	public void test_IsChildOf() {
		assertTrue(classException.isChildOf(classObject));
		assertFalse(classObject.isChildOf(classException));
		
		assertTrue(classException.isChildOf(classThrowable));
		assertFalse(classThrowable.isChildOf(classException));
		
		assertTrue(classList.isChildOf(classObject));
	}
	
	@Test
	public void test_CommonParent() {
		assertEquals(classThrowable, classException.getCommonParent(classError));
		assertEquals(classThrowable, classError.getCommonParent(classException));
		
		assertEquals(classThrowable, classException.getCommonParent(classThrowable));
		assertEquals(classThrowable, classThrowable.getCommonParent(classException));
		
		assertEquals(classObject, classException.getCommonParent(classList));
		assertEquals(classObject, classList.getCommonParent(classException));
	}
}
