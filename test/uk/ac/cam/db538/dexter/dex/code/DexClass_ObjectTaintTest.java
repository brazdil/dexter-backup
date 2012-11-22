package uk.ac.cam.db538.dexter.dex.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexField;

public class DexClass_ObjectTaintTest {

  @Test
  public void testGeneratesClassType_FirstWorks() {
    val dex = new Dex();
    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val clsTaint_Type = clsTaint.getType();

    assertEquals("Lt/$0;", clsTaint_Type.getDescriptor());
  }

  @Test
  public void testGeneratesClassType_SecondWorks() {
    val dex = new Dex();

    // reference the 0th descriptor
    // this makes the class think it is used inside of the bytecode
    // and therefore it cannot assign it
    dex.getParsingCache().getClassType("Lt/$0;");

    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val clsTaint_Type = clsTaint.getType();

    assertEquals("Lt/$1;", clsTaint_Type.getDescriptor());
  }

  @Test
  public void testHasObjectMap() {
    val dex = new Dex();
    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val clsTaint_Fields = clsTaint.getFields();

    DexField objMapField = null;
    for (val f : clsTaint_Fields)
      if (f.getName().equals("obj_map"))
        objMapField = f;

    assertTrue(objMapField != null);
    assertEquals("Ljava/util/WeakHashMap;", objMapField.getType().getDescriptor());
  }
}
