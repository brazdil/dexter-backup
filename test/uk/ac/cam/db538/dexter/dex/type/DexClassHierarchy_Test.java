package uk.ac.cam.db538.dexter.dex.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.type.hierarchy.ClassHierarchyException;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.DexClassHierarchy;

public class DexClassHierarchy_Test {

  @Test(expected=ClassHierarchyException.class)
  public void testAddClass_Multiple() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
  }

  @Test
  public void testCheckConsistency_Valid() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeClassB = DexType_Class.parse("Lcom/test/MyClassB;", cache);
    val typeClassC = DexType_Class.parse("Lcom/test/MyClassC;", cache);
    val typeInterface1 = DexType_Class.parse("Lcom/test/MyInterface1;", cache);

    val interfaces = new HashSet<DexType_Class>();
    interfaces.add(typeInterface1);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassB, typeClassA, interfaces);
    hierarchy.addClass(typeClassC, typeClassA);
    hierarchy.addInterface(typeInterface1);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_MissingLinks() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeClassB = DexType_Class.parse("Lcom/test/MyClassB;", cache);
    val typeClassC = DexType_Class.parse("Lcom/test/MyClassC;", cache);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
//		hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassB, typeClassA);
    hierarchy.addClass(typeClassC, typeClassA);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_RootInterface() {
    val cache = new DexTypeCache();

    val typeObject1 = DexType_Class.parse("Ljava/lang/Object1;", cache);
    val typeObject2 = DexType_Class.parse("Ljava/lang/Object2;", cache);

    val hierarchy = new DexClassHierarchy(typeObject1);
    hierarchy.addInterface(typeObject1);
    hierarchy.addClass(typeObject2, typeObject1);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_InterfaceNotExtendingRoot() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeInterface1 = DexType_Class.parse("Lcom/test/MyInterface1;", cache);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addMember(typeInterface1, typeClassA, null, null, true);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_ClassMissingInterface() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeInterface1 = DexType_Class.parse("Lcom/test/MyInterface1;", cache);

    val interfaces = new HashSet<DexType_Class>();
    interfaces.add(typeInterface1);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject, interfaces);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_ClassImplementingClass() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeClassB = DexType_Class.parse("Lcom/test/MyClassB;", cache);

    val interfaces = new HashSet<DexType_Class>();
    interfaces.add(typeClassB);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject, interfaces);
    hierarchy.addClass(typeClassB, typeObject);

    hierarchy.checkConsistency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testAddMember_Loop_MultipleElement() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeClassB = DexType_Class.parse("Lcom/test/MyClassB;", cache);
    val typeClassC = DexType_Class.parse("Lcom/test/MyClassC;", cache);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);

    hierarchy.addClass(typeClassA, typeClassC);
    hierarchy.addClass(typeClassB, typeClassA);
    hierarchy.addClass(typeClassC, typeClassB);
  }

  @Test(expected=ClassHierarchyException.class)
  public void testAddMember_Loop_SingleElement() {
    val cache = new DexTypeCache();

    val typeObject1 = DexType_Class.parse("Ljava/lang/Object1;", cache);
    val typeObject2 = DexType_Class.parse("Ljava/lang/Object2;", cache);

    val hierarchy = new DexClassHierarchy(typeObject1);
    hierarchy.addClass(typeObject1, typeObject1);
    hierarchy.addClass(typeObject2, typeObject2);
  }

  @Test
  public void testIsAncestor() {
    val cache = new DexTypeCache();

    val typeObject = DexType_Class.parse("Ljava/lang/Object;", cache);
    val typeClassA = DexType_Class.parse("Lcom/test/MyClassA;", cache);
    val typeClassB = DexType_Class.parse("Lcom/test/MyClassB;", cache);
    val typeClassC = DexType_Class.parse("Lcom/test/MyClassC;", cache);

    val hierarchy = new DexClassHierarchy(typeObject);
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassB, typeClassA);
    hierarchy.addClass(typeClassC, typeClassA);

    assertTrue(hierarchy.isAncestor(typeObject, typeObject));

    assertTrue(hierarchy.isAncestor(typeClassA, typeObject));
    assertTrue(hierarchy.isAncestor(typeClassB, typeObject));
    assertTrue(hierarchy.isAncestor(typeClassC, typeObject));

    assertFalse(hierarchy.isAncestor(typeObject, typeClassA));
    assertFalse(hierarchy.isAncestor(typeObject, typeClassB));
    assertFalse(hierarchy.isAncestor(typeObject, typeClassC));

    assertTrue(hierarchy.isAncestor(typeClassB, typeClassA));
    assertTrue(hierarchy.isAncestor(typeClassC, typeClassA));

    assertFalse(hierarchy.isAncestor(typeClassA, typeClassB));
    assertFalse(hierarchy.isAncestor(typeClassA, typeClassC));

    assertFalse(hierarchy.isAncestor(typeClassC, typeClassB));
    assertFalse(hierarchy.isAncestor(typeClassB, typeClassC));
  }
}
