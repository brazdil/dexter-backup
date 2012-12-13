package uk.ac.cam.db538.dexter.dex.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.ClassHierarchyException;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.DexClassHierarchy;

public class DexClassHierarchy_Test {

  @Test(expected=ClassHierarchyException.class)
  public void testAddClass_Multiple() {
    val cache = new DexParsingCache();

    val typeObject = cache.getClassType("Ljava/lang/Object;");
    val typeClassA = cache.getClassType("Lcom/test/MyClassA;");

    val hierarchy = new DexClassHierarchy();
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
  }

  @Test
  public void testCheckConsistency_Valid() {
    val cache = new DexParsingCache();

    val typeObject = cache.getClassType("Ljava/lang/Object;");
    val typeClassA = cache.getClassType("Lcom/test/MyClassA;");
    val typeClassB = cache.getClassType("Lcom/test/MyClassB;");
    val typeClassC = cache.getClassType("Lcom/test/MyClassC;");

    val hierarchy = new DexClassHierarchy();
    hierarchy.addClass(typeObject, typeObject);
    hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassB, typeClassA);
    hierarchy.addClass(typeClassC, typeClassA);

    hierarchy.checkConsistentency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_MissingLinks() {
    val cache = new DexParsingCache();

    val typeObject = cache.getClassType("Ljava/lang/Object;");
    val typeClassA = cache.getClassType("Lcom/test/MyClassA;");
    val typeClassB = cache.getClassType("Lcom/test/MyClassB;");
    val typeClassC = cache.getClassType("Lcom/test/MyClassC;");

    val hierarchy = new DexClassHierarchy();
    hierarchy.addClass(typeObject, typeObject);
//		hierarchy.addClass(typeClassA, typeObject);
    hierarchy.addClass(typeClassB, typeClassA);
    hierarchy.addClass(typeClassC, typeClassA);

    hierarchy.checkConsistentency();
  }

  @Test(expected=ClassHierarchyException.class)
  public void testCheckConsistency_Invalid_MultipleRoots() {
    val cache = new DexParsingCache();

    val typeObject1 = cache.getClassType("Ljava/lang/Object1;");
    val typeObject2 = cache.getClassType("Ljava/lang/Object2;");

    val hierarchy = new DexClassHierarchy();
    hierarchy.addClass(typeObject1, typeObject1);
    hierarchy.addClass(typeObject2, typeObject2);

    hierarchy.checkConsistentency();
  }

  @Test
  public void testIsAncestor() {
    val cache = new DexParsingCache();

    val typeObject = cache.getClassType("Ljava/lang/Object;");
    val typeClassA = cache.getClassType("Lcom/test/MyClassA;");
    val typeClassB = cache.getClassType("Lcom/test/MyClassB;");
    val typeClassC = cache.getClassType("Lcom/test/MyClassC;");

    val hierarchy = new DexClassHierarchy();
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
