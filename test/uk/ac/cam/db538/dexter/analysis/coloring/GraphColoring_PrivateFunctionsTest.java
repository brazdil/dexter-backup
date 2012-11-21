package uk.ac.cam.db538.dexter.analysis.coloring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.analysis.ClashGraph;
import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring.GcColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.utils.Pair;

public class GraphColoring_PrivateFunctionsTest {

  // GET NODE FOLLOW-UP RUN

  @SuppressWarnings("unchecked")
  private static LinkedList<Pair<DexRegister, GcColorRange>> execGetNodeFollowUpRun(DexRegister node, Set<LinkedList<DexRegister>> nodeFollowUps, Map<DexRegister, GcColorRange> nodeRanges) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("getNodeFollowUpRun", DexRegister.class, Set.class, Map.class);
      m.setAccessible(true);
      return (LinkedList<Pair<DexRegister, GcColorRange>>) m.invoke(null, node, nodeFollowUps, nodeRanges);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test(expected=RuntimeException.class)
  public void testGetNodeFollowUpRun_Empty() throws Throwable {
    execGetNodeFollowUpRun(
      new DexRegister(),
      new HashSet<LinkedList<DexRegister>>(),
      new HashMap<DexRegister, GcColorRange>());
  }

  @Test
  public void testGetNodeFollowUpRun_NonEmpty() throws Throwable {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();

    val run1 = new LinkedList<DexRegister>();
    run1.add(r1);
    val run2 = new LinkedList<DexRegister>();
    run2.add(r2);
    run2.add(r3);

    val nodeFollowUps = new HashSet<LinkedList<DexRegister>>();
    nodeFollowUps.add(run1);
    nodeFollowUps.add(run2);

    val nodeRanges = new HashMap<DexRegister, GcColorRange>();
    nodeRanges.put(r1, GcColorRange.Range_0_15);
    nodeRanges.put(r2, GcColorRange.Range_0_255);

    val nodeRun1 = execGetNodeFollowUpRun(r1, nodeFollowUps, nodeRanges);
    val nodeRun2 = execGetNodeFollowUpRun(r2, nodeFollowUps, nodeRanges);
    val nodeRun3 = execGetNodeFollowUpRun(r3, nodeFollowUps, nodeRanges);

    assertEquals(1, nodeRun1.size());
    assertEquals(r1, nodeRun1.get(0).getValA());
    assertEquals(GcColorRange.Range_0_15, nodeRun1.get(0).getValB());

    assertEquals(nodeRun2, nodeRun3);
    assertEquals(2, nodeRun2.size());
    assertEquals(r2, nodeRun2.get(0).getValA());
    assertEquals(GcColorRange.Range_0_255, nodeRun2.get(0).getValB());
    assertEquals(r3, nodeRun2.get(1).getValA());
    assertEquals(GcColorRange.Range_0_65535, nodeRun2.get(1).getValB());
  }

  // GET STRICTEST COLOR RANGE

  private static GcColorRange execGetStrictestColorRange(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("getStrictestColorRange", LinkedList.class);
      m.setAccessible(true);
      return (GcColorRange) m.invoke(null, nodeRun);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testGetStrictestColorRange_Empty() {
    assertEquals(GcColorRange.Range_0_65535, execGetStrictestColorRange(new LinkedList<Pair<DexRegister, GcColorRange>>()));
  }

  @Test
  public void testGetStrictestColorRange_NonEmpty() {
    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_255));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_15));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_15));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_255));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(new DexRegister(), GcColorRange.Range_0_65535));

    assertEquals(GcColorRange.Range_0_15, execGetStrictestColorRange(nodeRun));
  }

  // GET COLORS USED BY RUN NEIGHBOURS

  @SuppressWarnings("unchecked")
  private static Map<DexRegister, Set<Integer>> execGetColorsUsedByRunNeighbours(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, Map<DexRegister, Integer> coloringState, ClashGraph clashGraph) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("getColorsUsedByRunNeighbours", LinkedList.class, Map.class, ClashGraph.class);
      m.setAccessible(true);
      return (Map<DexRegister, Set<Integer>>) m.invoke(null, nodeRun, coloringState, clashGraph);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testGetColorsUsedByRunNeighbours_Empty() {
    assertTrue(
      execGetColorsUsedByRunNeighbours(
        new LinkedList<Pair<DexRegister, GcColorRange>>(),
        new HashMap<DexRegister, Integer>(),
        null).isEmpty());
  }

  @Test
  public void testGetColorsUsedByRunNeighbours_NonEmpty() {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();
    val r4 = new DexRegister();
    val r5 = new DexRegister();
    val r6 = new DexRegister();
    val r7 = new DexRegister();

    // put r1, r2, r3 into the same run
    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r1, GcColorRange.Range_0_15));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r2, GcColorRange.Range_0_15));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r3, GcColorRange.Range_0_15));

    // let r1 clash with r4,r7, r2 with r5 and r3 with r6
    val code = new DexCode();
    code.add(new DexInstruction_BinaryOp(code, r1, r1, r4, Opcode_BinaryOp.AddInt));
    code.add(new DexInstruction_ReturnVoid(code));
    code.add(new DexInstruction_BinaryOp(code, r1, r1, r7, Opcode_BinaryOp.AddInt));
    code.add(new DexInstruction_ReturnVoid(code));
    code.add(new DexInstruction_BinaryOp(code, r2, r2, r5, Opcode_BinaryOp.AddInt));
    code.add(new DexInstruction_ReturnVoid(code));
    code.add(new DexInstruction_BinaryOp(code, r3, r3, r6, Opcode_BinaryOp.AddInt));

    // set colors for r4,r7 and r5, but not r6
    val coloringState = new HashMap<DexRegister, Integer>();
    coloringState.put(r4, 12);
    coloringState.put(r5, 24);
    coloringState.put(r7, 9);

    // check the outcome
    val usedColors = execGetColorsUsedByRunNeighbours(nodeRun, coloringState, new ClashGraph(code));
    assertEquals(2, usedColors.size());

    val usedColors1 = usedColors.get(r1);
    val usedColors2 = usedColors.get(r2);
    val usedColors3 = usedColors.get(r3);

    assertEquals(2, usedColors1.size());
    assertTrue(usedColors1.contains(12));
    assertTrue(usedColors1.contains(9));

    assertEquals(1, usedColors2.size());
    assertTrue(usedColors2.contains(24));

    assertEquals(null, usedColors3);
  }

  // FIRST UNUSED HIGHER OR EQUAL COLOR

  private static int execFirstUnusedHigherOrEqualColor(int color, int[] sortedColors) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("firstUnusedHigherOrEqualColor", int.class, int[].class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, color, sortedColors);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return -2;
    }
  }

  @Test
  public void testFirstUnusedHigherOrEqualColor_Higher() {
    val sortedArray = new int[3];
    sortedArray[0] = 2;
    sortedArray[1] = 3;
    sortedArray[2] = 4;

    assertEquals(5, execFirstUnusedHigherOrEqualColor(2, sortedArray));
  }

  @Test
  public void testFirstUnusedHigherOrEqualColor_Equal() {
    val sortedArray = new int[2];
    sortedArray[0] = 3;
    sortedArray[1] = 4;

    assertEquals(2, execFirstUnusedHigherOrEqualColor(2, sortedArray));
    assertEquals(5, execFirstUnusedHigherOrEqualColor(5, sortedArray));
  }

  @Test
  public void testFirstUnusedHigherOrEqualColor_Overflow() {
    val sortedArray = new int[3];
    sortedArray[0] = 65533;
    sortedArray[1] = 65534;
    sortedArray[2] = 65535;

    assertEquals(-1, execFirstUnusedHigherOrEqualColor(65533, sortedArray));
  }

  // NEXT USED HIGHER COLOR

  private static int execNextUsedHigherColor(int color, int[] sortedColors) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("nextUsedHigherColor", int.class, int[].class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, color, sortedColors);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return -2;
    }
  }

  @Test
  public void testNextUsedHigherColor_Standard() {
    val sortedArray = new int[3];
    sortedArray[0] = 2;
    sortedArray[1] = 36;
    sortedArray[2] = 48;

    assertEquals(2, execNextUsedHigherColor(0, sortedArray));
    assertEquals(2, execNextUsedHigherColor(1, sortedArray));
    assertEquals(36, execNextUsedHigherColor(2, sortedArray));
    assertEquals(36, execNextUsedHigherColor(3, sortedArray));
    assertEquals(48, execNextUsedHigherColor(36, sortedArray));
    assertEquals(48, execNextUsedHigherColor(37, sortedArray));
    assertEquals(48, execNextUsedHigherColor(47, sortedArray));
    assertEquals(65536, execNextUsedHigherColor(48, sortedArray));
    assertEquals(65536, execNextUsedHigherColor(49, sortedArray));
  }

  @Test
  public void testFirstUnusedHigherColor_HittingMax() {
    val sortedArray = new int[3];
    sortedArray[0] = 2;
    sortedArray[1] = 36;
    sortedArray[2] = 70000;

    assertEquals(65536, execNextUsedHigherColor(36, sortedArray));
    assertEquals(65536, execNextUsedHigherColor(37, sortedArray));
  }

  // GENERATE COLORS IN RANGE

  private static int execGenerateColorsInRange(int low, int high, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, Map<DexRegister, int[]> sortedForbiddenColors) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateColorsInRange", int.class, int.class, LinkedList.class, Map.class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, low, high, nodeRun, sortedForbiddenColors);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return -2;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_MaxOverflow() throws Throwable {
    val node1 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));

    val sortedArray = new int[3];
    sortedArray[0] = 65533;
    sortedArray[1] = 65534;
    sortedArray[2] = 65535;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray);

    execGenerateColorsInRange(65534, 70000, nodeRun, sortedMap);
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_HighOverflow() throws Throwable {
    val node1 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));

    val sortedArray = new int[3];
    sortedArray[0] = 2;
    sortedArray[1] = 3;
    sortedArray[2] = 4;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray);

    execGenerateColorsInRange(3, 4, nodeRun, sortedMap);
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_Constraints_0_15() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_15));

    val sortedArray1 = new int[3];
    sortedArray1[0] = 12;
    sortedArray1[1] = 13;
    sortedArray1[2] = 14;

    val sortedArray2 = new int[3];
    sortedArray2[0] = 14;
    sortedArray2[1] = 17;
    sortedArray2[2] = 18;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray1);
    sortedMap.put(node2, sortedArray2);

    execGenerateColorsInRange(12, 255, nodeRun, sortedMap);
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_Constraints_0_255() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_255));

    val sortedArray1 = new int[3];
    sortedArray1[0] = 252;
    sortedArray1[1] = 253;
    sortedArray1[2] = 254;

    val sortedArray2 = new int[3];
    sortedArray2[0] = 254;
    sortedArray2[1] = 257;
    sortedArray2[2] = 258;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray1);
    sortedMap.put(node2, sortedArray2);

    execGenerateColorsInRange(252, 300, nodeRun, sortedMap);
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_Constraints_0_65535() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_65535));

    val sortedArray = new int[3];
    sortedArray[0] = 65532;
    sortedArray[1] = 65533;
    sortedArray[2] = 65534;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray);
    sortedMap.put(node2, sortedArray);

    execGenerateColorsInRange(65532, 70000, nodeRun, sortedMap);
  }

  @Test
  public void testGenerateColorsInRange_Colorable_FitsFirstGap() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_65535));

    val sortedArray1 = new int[5];
    sortedArray1[0] = 2;
    sortedArray1[1] = 4;
    sortedArray1[2] = 5;
    sortedArray1[3] = 8;
    sortedArray1[4] = 9;

    val sortedArray2 = new int[5];
    sortedArray2[0] = 2;
    sortedArray2[1] = 3;
    sortedArray2[2] = 5;
    sortedArray2[3] = 8;
    sortedArray2[4] = 9;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray1);
    sortedMap.put(node2, sortedArray2);

    assertEquals(3, execGenerateColorsInRange(2, 10, nodeRun, sortedMap));
  }

  @Test
  public void testGenerateColorsInRange_Colorable_TooLongForFirstGap() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_65535));

    val sortedArray1 = new int[6];
    sortedArray1[0] = 2;
    sortedArray1[1] = 4;
    sortedArray1[2] = 5;
    sortedArray1[3] = 7;
    sortedArray1[4] = 8;
    sortedArray1[5] = 9;

    val sortedArray2 = new int[6];
    sortedArray2[0] = 2;
    sortedArray2[1] = 4;
    sortedArray2[2] = 5;
    sortedArray2[3] = 6;
    sortedArray2[4] = 8;
    sortedArray2[5] = 9;

    val sortedMap = new HashMap<DexRegister, int[]>();
    sortedMap.put(node1, sortedArray1);
    sortedMap.put(node2, sortedArray2);

    assertEquals(6, execGenerateColorsInRange(2, 10, nodeRun, sortedMap));
  }

  // GENERATE COLORS

  private static int execGenerateColors(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, Map<DexRegister, Set<Integer>> forbiddenColors) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateColors", LinkedList.class, Map.class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, nodeRun, forbiddenColors);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return -2;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testGenerateColors_Colorable_Range_0_15() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_15));

    val forbiddenColors1 = new HashSet<Integer>();
    forbiddenColors1.add(2);
    forbiddenColors1.add(3);
    val forbiddenMap1 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap1.put(node1, forbiddenColors1);
    forbiddenMap1.put(node2, forbiddenColors1);

    assertEquals(0, execGenerateColors(nodeRun, forbiddenMap1));

    val forbiddenColors2 = new HashSet<Integer>();
    forbiddenColors2.add(1);
    forbiddenColors2.add(2);
    forbiddenColors2.add(3);
    forbiddenColors2.add(5);
    forbiddenColors2.add(6);
    val forbiddenMap2 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap2.put(node1, forbiddenColors2);
    forbiddenMap2.put(node2, forbiddenColors2);

    assertEquals(7, execGenerateColors(nodeRun, forbiddenMap2));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_Range_0_15() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_15));

    val forbiddenColors = new HashSet<Integer>();
    for (int i = 0; i <= 14; ++i)
      forbiddenColors.add(i);
    val forbiddenMap = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap.put(node1, forbiddenColors);
    forbiddenMap.put(node2, forbiddenColors);

    execGenerateColors(nodeRun, forbiddenMap);
  }

  @Test
  public void testGenerateColors_Colorable_Range_0_255() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_255));

    val forbiddenColors1 = new HashSet<Integer>();
    for (int i = 0; i <= 248; ++i)
      forbiddenColors1.add(i);
    val forbiddenMap1 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap1.put(node1, forbiddenColors1);
    forbiddenMap1.put(node2, forbiddenColors1);
    assertEquals(249, execGenerateColors(nodeRun, forbiddenMap1));

    val forbiddenColors2 = new HashSet<Integer>();
    forbiddenColors2.add(0);
    forbiddenColors2.add(1);
    for (int i = 14; i <= 300; ++i)
      forbiddenColors2.add(i);
    val forbiddenMap2 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap2.put(node1, forbiddenColors2);
    forbiddenMap2.put(node2, forbiddenColors2);

    assertEquals(2, execGenerateColors(nodeRun, forbiddenMap2));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_Range_0_255() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_255));

    val forbiddenColors = new HashSet<Integer>();
    for (int i = 0; i <= 254; ++i)
      forbiddenColors.add(i);
    val forbiddenMap = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap.put(node1, forbiddenColors);
    forbiddenMap.put(node2, forbiddenColors);

    execGenerateColors(nodeRun, forbiddenMap);
  }

  @Test
  public void testGenerateColors_Colorable_Range_0_65535() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_65535));

    val forbiddenColors1 = new HashSet<Integer>();
    for (int i = 0; i <= 45000; ++i)
      forbiddenColors1.add(i);
    val forbiddenMap1 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap1.put(node1, forbiddenColors1);
    forbiddenMap1.put(node2, forbiddenColors1);
    assertEquals(45001, execGenerateColors(nodeRun, forbiddenMap1));

    val forbiddenColors2 = new HashSet<Integer>();
    for (int i = 0; i <= 200; ++i)
      forbiddenColors2.add(i);
    for (int i = 250; i <= 65535; ++i)
      forbiddenColors2.add(i);
    val forbiddenMap2 = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap2.put(node1, forbiddenColors2);
    forbiddenMap2.put(node2, forbiddenColors2);

    assertEquals(201, execGenerateColors(nodeRun, forbiddenMap2));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_Range_0_65535() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node1, GcColorRange.Range_0_65535));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(node2, GcColorRange.Range_0_65535));

    val forbiddenColors = new HashSet<Integer>();
    for (int i = 0; i <= 254; ++i)
      forbiddenColors.add(i);
    for (int i = 256; i <= 65534; ++i)
      forbiddenColors.add(i);
    val forbiddenMap = new HashMap<DexRegister, Set<Integer>>();
    forbiddenMap.put(node1, forbiddenColors);
    forbiddenMap.put(node2, forbiddenColors);

    execGenerateColors(nodeRun, forbiddenMap);
  }

  // CONTAINS ANY OF NODES

  private static boolean execContainsAnyOfNodes(Collection<DexRegister> collection, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("containsAnyOfNodes", Collection.class, LinkedList.class);
      m.setAccessible(true);
      return (Boolean) m.invoke(null, collection, nodeRun);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return false;
    }
  }

  @Test
  public void testContainsAnyOfNodes() {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();
    val r4 = new DexRegister();

    val collection = new LinkedList<DexRegister>();
    collection.add(r1);
    collection.add(r2);

    val run1 = new LinkedList<Pair<DexRegister, GcColorRange>>();
    run1.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r1, GcColorRange.Range_0_65535));
    run1.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r3, GcColorRange.Range_0_65535));

    val run2 = new LinkedList<Pair<DexRegister, GcColorRange>>();
    run2.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r3, GcColorRange.Range_0_65535));
    run2.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r4, GcColorRange.Range_0_65535));

    assertTrue(execContainsAnyOfNodes(collection, run1));
    assertFalse(execContainsAnyOfNodes(collection, run2));
  }

  // GENERATE CODE WITH SPILLED NODE

  private static DexCode execGenerateCodeWithSpilledNode(DexCode currentCode, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateCodeWithSpilledNode", DexCode.class, LinkedList.class);
      m.setAccessible(true);
      return (DexCode) m.invoke(null, currentCode, nodeRun);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testGenerateCodeWithSpilledNode_Empty() {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();
    val r4 = new DexRegister();

    val code = new DexCode();

    val i1 = new DexInstruction_BinaryOp(code, r1, r1, r1, Opcode_BinaryOp.AddInt);
    val i2 = new DexInstruction_BinaryOp(code, r2, r2, r2, Opcode_BinaryOp.AddInt);
    val i3 = new DexInstruction_BinaryOp(code, r3, r3, r3, Opcode_BinaryOp.AddInt);
    val i4 = new DexInstruction_BinaryOp(code, r4, r4, r4, Opcode_BinaryOp.AddInt);

    code.add(i1);
    code.add(i2);
    code.add(i3);
    code.add(i4);

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
//	  nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r2, GcColorRange.Range_0_255));
//	  nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r3, GcColorRange.Range_0_255));

    val newCode = execGenerateCodeWithSpilledNode(code, nodeRun);
    assertEquals(4, newCode.getInstructionList().size());
    assertEquals(i1, newCode.getInstructionList().get(0));
    assertEquals(i2, newCode.getInstructionList().get(1));
    assertEquals(i3, newCode.getInstructionList().get(2));
    assertEquals(i4, newCode.getInstructionList().get(3));
  }

  @Test
  public void testGenerateCodeWithSpilledNode_Standard() {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();
    val r4 = new DexRegister();

    val code = new DexCode();

    val i1 = new DexInstruction_BinaryOp(code, r1, r1, r1, Opcode_BinaryOp.AddInt);
    val i2 = new DexInstruction_BinaryOp(code, r4, r2, r2, Opcode_BinaryOp.AddInt); // only in referenced registers
    val i3 = new DexInstruction_BinaryOp(code, r3, r4, r4, Opcode_BinaryOp.AddInt); // only in defined registers
    val i4 = new DexInstruction_BinaryOp(code, r4, r4, r4, Opcode_BinaryOp.AddInt);

    code.add(i1);
    code.add(i2);
    code.add(i3);
    code.add(i4);

    val nodeRun = new LinkedList<Pair<DexRegister, GcColorRange>>();
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r2, GcColorRange.Range_0_255));
    nodeRun.add(new Pair<DexRegister, GraphColoring.GcColorRange>(r3, GcColorRange.Range_0_255));

    val newCode = execGenerateCodeWithSpilledNode(code, nodeRun);
    val insns = newCode.getInstructionList();
    assertTrue(insns.contains(i1));
    assertFalse(insns.contains(i2));
    assertFalse(insns.contains(i3));
    assertTrue(insns.contains(i4));
  }

  // REMOVE GAPS FROM COLORING

  @SuppressWarnings("unchecked")
  private static Map<DexRegister, Integer> execRemoveGapsFromColoring(Map<DexRegister, Integer> oldColoring) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("removeGapsFromColoring", Map.class);
      m.setAccessible(true);
      return (Map<DexRegister, Integer>) m.invoke(null, oldColoring);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testRemoveGapsFromColoring_Empty() {
    val emptyColoring = new HashMap<DexRegister, Integer>();
    val newColoring = execRemoveGapsFromColoring(emptyColoring);
    assertTrue(emptyColoring.isEmpty());
    assertTrue(newColoring.isEmpty());
  }

  @Test
  public void testRemoveGapsFromColoring_NonEmpty() {
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();
    val r4 = new DexRegister();

    val oldColoring = new HashMap<DexRegister, Integer>();
    oldColoring.put(r1, 22);
    oldColoring.put(r2, 48);
    oldColoring.put(r3, 48);
    oldColoring.put(r4, 35);

    val newColoring = execRemoveGapsFromColoring(oldColoring);
    assertEquals(4, newColoring.size());
    assertEquals(Integer.valueOf(0), newColoring.get(r1));
    assertEquals(Integer.valueOf(2), newColoring.get(r2));
    assertEquals(Integer.valueOf(2), newColoring.get(r3));
    assertEquals(Integer.valueOf(1), newColoring.get(r4));
  }

}
