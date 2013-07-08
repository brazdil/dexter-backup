package uk.ac.cam.db538.dexter.hierarchy;

import static org.junit.Assert.*;

import java.io.File;

import lombok.val;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class ClassDefinition_Test {

	private RuntimeHierarchy hierarchy;
	private DexTypeCache typeCache;
	
	private ClassDefinition classObject;
	private InterfaceDefinition classList;
	private ClassDefinition classArrayList;
	private ClassDefinition classLinkedList;
	private InterfaceDefinition classMap;
	private ClassDefinition classHashMap;
	
	@Before
	public void setUp() throws Exception {
		hierarchy = HierarchyBuilder.deserialize(new File("test/hierarchy.dump")).build();
		typeCache = hierarchy.getTypeCache();
		
		val typeObject = DexClassType.parse("Ljava/lang/Object;", typeCache);
		classObject = hierarchy.getClassDefinition(typeObject);

		val typeList = DexClassType.parse("Ljava/util/List;", typeCache);
		classList = hierarchy.getInterfaceDefinition(typeList);

		val typeArrayList = DexClassType.parse("Ljava/util/ArrayList;", typeCache);
		classArrayList = hierarchy.getClassDefinition(typeArrayList);

		val typeLinkedList = DexClassType.parse("Ljava/util/LinkedList;", typeCache);
		classLinkedList = hierarchy.getClassDefinition(typeLinkedList);

		val typeMap = DexClassType.parse("Ljava/util/Map;", typeCache);
		classMap = hierarchy.getInterfaceDefinition(typeMap);

		val typeHashMap = DexClassType.parse("Ljava/util/HashMap;", typeCache);
		classHashMap = hierarchy.getClassDefinition(typeHashMap);
	}

	@Test
	public void test_ImplementsInterface_List() {
		assertFalse(classObject.implementsInterface(classList));
		assertTrue(classArrayList.implementsInterface(classList));
		assertTrue(classLinkedList.implementsInterface(classList));
		assertFalse(classHashMap.implementsInterface(classList));
	}

	@Test
	public void test_ImplementsInterface_Map() {
		assertFalse(classObject.implementsInterface(classMap));
		assertFalse(classArrayList.implementsInterface(classMap));
		assertFalse(classLinkedList.implementsInterface(classMap));
		assertTrue(classHashMap.implementsInterface(classMap));
	}
}