package uk.ac.cam.db538.dexter.dex.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Util.AccessFlags;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.Dex;

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
  public void testHasField_ObjectMap() {
    val dex = new Dex();
    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val field = clsTaint.getField_ObjectMap();

    assertTrue(field != null);
    assertTrue(clsTaint.getFields().contains(field));

    assertEquals("Ljava/util/WeakHashMap;", field.getType().getDescriptor());
  }

  @Test
  public void testHasMethod_Clinit() {
    val dex = new Dex();
    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val method = clsTaint.getMethod_Clinit();

    assertTrue(method != null);
    assertTrue(clsTaint.getMethods().contains(method));

    assertTrue(method.getAccessFlagSet().contains(AccessFlags.STATIC));
    assertTrue(method.getAccessFlagSet().contains(AccessFlags.CONSTRUCTOR));
  }

  @Test
  public void testHasMethod_Init() {
    val dex = new Dex();
    dex.instrument();

    val clsTaint = dex.getObjectTaintClass();
    val method = clsTaint.getMethod_Init();

    assertTrue(method != null);
    assertTrue(clsTaint.getMethods().contains(method));

    assertTrue(method.getAccessFlagSet().contains(AccessFlags.PRIVATE));
    assertTrue(method.getAccessFlagSet().contains(AccessFlags.CONSTRUCTOR));
  }
}
