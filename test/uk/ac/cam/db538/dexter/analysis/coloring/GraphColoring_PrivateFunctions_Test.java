package uk.ac.cam.db538.dexter.analysis.coloring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.analysis.ClashGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpWide;
import uk.ac.cam.db538.dexter.utils.Pair;

public class GraphColoring_PrivateFunctions_Test {

  private static void genClash(DexCode code, DexRegister reg1, DexRegister reg2) {
    code.add(new DexInstruction_BinaryOp(code, reg1, reg1, reg2, Opcode_BinaryOp.AddInt));
    code.add(new DexInstruction_ReturnVoid(code));
  }

  private static DexRegister[] genRegisters(int count) {
    val regs = new DexRegister[count];
    for (int i = 0; i < count; ++i)
      regs[i] = new DexRegister();
    return regs;
  }

  // GENERATE NODE STATES

  private static NodeStatesMap execGenerateNodeStates(DexCode code) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateNodeStates", DexCode.class);
      m.setAccessible(true);
      return (NodeStatesMap) m.invoke(null, code);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testGenerateNodeStates_Empty() throws Throwable {
    val code = new DexCode();
    val nodeMap = execGenerateNodeStates(code);
    assertTrue(nodeMap.isEmpty());
  }

  @Test
  public void testGenerateNodeStates_NonEmpty() throws Throwable {
    val code = new DexCode();

    val r0 = new DexRegister();
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();

    // r0 unconstrained but used
    code.add(new DexInstruction_Move(code, r0, r0, false));

    // constraint: r1 in range (0-255)
    code.add(new DexInstruction_BinaryOp(code, r1, r1, r1, Opcode_BinaryOp.AddInt));

    // constraint: r2 - > r3
    code.add(new DexInstruction_BinaryOpWide(code, r2, r3, r2, r3, r2, r3, Opcode_BinaryOpWide.AddLong));

    val nodeMap = execGenerateNodeStates(code);

    val n0 = nodeMap.get(r0);
    val n1 = nodeMap.get(r1);
    val n2 = nodeMap.get(r2);
    val n3 = nodeMap.get(r3);

    val run0 = new NodeRun();
    run0.add(r0);
    val run1 = new NodeRun();
    run1.add(r1);
    val run23 = new NodeRun();
    run23.add(r2);
    run23.add(r3);

    assertEquals(ColorRange.RANGE_16BIT, n0.getColorRange());
    assertEquals(run0, n0.getNodeRun());

    assertEquals(ColorRange.RANGE_8BIT, n1.getColorRange());
    assertEquals(run1, n1.getNodeRun());

    assertEquals(ColorRange.RANGE_8BIT, n2.getColorRange());
    assertEquals(run23, n2.getNodeRun());

    assertEquals(ColorRange.RANGE_16BIT, n3.getColorRange());
    assertEquals(run23, n3.getNodeRun());
  }

  // GENERATE RUN FORBIDDEN COLORS

  private static void execGenerateRunForbiddenColors(NodeRun nodeRun, NodeStatesMap nodeMap, ClashGraph clashGraph) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateRunForbiddenColors", NodeRun.class, NodeStatesMap.class, ClashGraph.class);
      m.setAccessible(true);
      m.invoke(null, nodeRun, nodeMap, clashGraph);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testGenerateRunForbiddenColors_Empty() throws Throwable {
    val code = new DexCode();
    val nodeMap = execGenerateNodeStates(code);
    val clashGraph = new ClashGraph(code);
    val nodeRun = new NodeRun();

    execGenerateRunForbiddenColors(nodeRun, nodeMap, clashGraph);

    // nothing should happen,
    // but it also shouldn't crash
  }

  @Test
  public void testGenerateRunForbiddenColors_NonEmpty() throws Throwable {
    val code = new DexCode();
    val r = genRegisters(6);

    val nodeRun = new NodeRun();
    nodeRun.add(r[0]);
    nodeRun.add(r[1]);
    nodeRun.add(r[2]);
    nodeRun.add(r[3]);

    genClash(code, r[0], r[4]);
    genClash(code, r[1], r[4]);
    genClash(code, r[1], r[5]);
    genClash(code, r[2], r[1]);
    genClash(code, r[2], r[4]);
    genClash(code, r[2], r[5]);
    genClash(code, r[3], r[3]);

    val nodeMap = execGenerateNodeStates(code);
    nodeMap.get(r[4]).setColor(6);
    nodeMap.get(r[5]).setColor(2);

    val clashGraph = new ClashGraph(code);
    execGenerateRunForbiddenColors(nodeRun, nodeMap, clashGraph);

    val c0 = nodeMap.get(r[0]).getForbiddenColors();
    val c1 = nodeMap.get(r[1]).getForbiddenColors();
    val c2 = nodeMap.get(r[2]).getForbiddenColors();
    val c3 = nodeMap.get(r[3]).getForbiddenColors();
    val c4 = nodeMap.get(r[4]).getForbiddenColors();
    val c5 = nodeMap.get(r[5]).getForbiddenColors();

    assertEquals(1, c0.length);
    assertEquals(6, c0[0]);

    assertEquals(2, c1.length);
    assertEquals(2, c1[0]);
    assertEquals(6, c1[1]);

    assertEquals(2, c2.length);
    assertEquals(2, c2[0]);
    assertEquals(6, c2[1]);

    assertEquals(0, c3.length);

    assertEquals(null, c4);
    assertEquals(null, c5);
  }


  // GET STRICTEST COLOR RANGE

  private static ColorRange execGetStrictestColorRange(NodeRun nodeRun, NodeStatesMap nodeMap) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("getStrictestColorRange", NodeRun.class, NodeStatesMap.class);
      m.setAccessible(true);
      return (ColorRange) m.invoke(null, nodeRun, nodeMap);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testGetStrictestColorRange_Empty() {
    assertEquals(ColorRange.RANGE_16BIT, execGetStrictestColorRange(new NodeRun(), null));
  }

  @Test
  public void testGetStrictestColorRange_NonEmpty() {
    val nodeRun = new NodeRun();
    val nodeMap = new NodeStatesMap();

    val r = genRegisters(6);

    for (val reg : r)
      nodeRun.add(reg);

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_8BIT, nodeRun));
    nodeMap.put(r[2], new NodeState(ColorRange.RANGE_4BIT, nodeRun));
    nodeMap.put(r[3], new NodeState(ColorRange.RANGE_4BIT, nodeRun));
    nodeMap.put(r[4], new NodeState(ColorRange.RANGE_8BIT, nodeRun));
    nodeMap.put(r[5], new NodeState(ColorRange.RANGE_16BIT, nodeRun));

    assertEquals(ColorRange.RANGE_4BIT, execGetStrictestColorRange(nodeRun, nodeMap));
  }

  // CHECK COLOR RANGE AVAILABLE

  private static boolean execCheckColorRange(int firstColor, NodeRun nodeRun, NodeStatesMap nodeMap) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("checkColorRange", int.class, NodeRun.class, NodeStatesMap.class);
      m.setAccessible(true);
      return (Boolean) m.invoke(null, firstColor, nodeRun, nodeMap);
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return false;
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testColorRangeAvailable_Positive() throws Throwable {
    val r = genRegisters(3);
    val nodeRun = new NodeRun(r);
    val nodeMap = new NodeStatesMap();

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 5, 6, 7 }));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 4, 6, 7 }));
    nodeMap.put(r[2], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 4, 5, 7 }));

    assertTrue(execCheckColorRange(4, nodeRun, nodeMap));
  }

  @Test
  public void testColorRangeAvailable_Negative() throws Throwable {
    val r = genRegisters(3);
    val nodeRun = new NodeRun(r);
    val nodeMap = new NodeStatesMap();

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 5, 6, 7 }));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 4, 6, 7 }));
    nodeMap.put(r[2], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 3, 4, 5, 6, 7 }));

    assertFalse(execCheckColorRange(4, nodeRun, nodeMap));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testColorRangeAvailable_ExceedsRange() throws Throwable {
    val r = genRegisters(3);
    val nodeRun = new NodeRun(r);
    val nodeMap = new NodeStatesMap();

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 13, 15, 16 }));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 13, 14, 16 }));
    nodeMap.put(r[2], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 13, 14, 15 }));

    execCheckColorRange(14, nodeRun, nodeMap);
  }

  // GENERATE COLORS IN RANGE

  private static int execGenerateColorsInRange(int low, int high, NodeRun nodeRun, NodeStatesMap nodeMap) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateColorsInRange", int.class, int.class, NodeRun.class, NodeStatesMap.class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, low, high, nodeRun, nodeMap);
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

    val nodeRun = new NodeRun();
    nodeRun.add(node1);

    val forbiddenColors = new int[3];
    forbiddenColors[0] = 65533;
    forbiddenColors[1] = 65534;
    forbiddenColors[2] = 65535;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(node1, new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors));

    execGenerateColorsInRange(65534, 70000, nodeRun, nodeMap);
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColorsInRange_Uncolorable_HighOverflow() throws Throwable {
    val node1 = new DexRegister();

    val nodeRun = new NodeRun();
    nodeRun.add(node1);

    val forbiddenColors = new int[3];
    forbiddenColors[0] = 2;
    forbiddenColors[1] = 3;
    forbiddenColors[2] = 4;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(node1, new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors));

    execGenerateColorsInRange(3, 4, nodeRun, nodeMap);
  }

  @Test
  public void testGenerateColorsInRange_Colorable_FitsFirstGap() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new NodeRun();
    nodeRun.add(node1);
    nodeRun.add(node2);

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

    val nodeMap = new NodeStatesMap();
    nodeMap.put(node1, new NodeState(ColorRange.RANGE_16BIT, nodeRun, sortedArray1));
    nodeMap.put(node2, new NodeState(ColorRange.RANGE_16BIT, nodeRun, sortedArray2));

    assertEquals(3, execGenerateColorsInRange(2, 10, nodeRun, nodeMap));
  }

  @Test
  public void testGenerateColorsInRange_Colorable_FitsSecondGap() throws Throwable {
    val node1 = new DexRegister();
    val node2 = new DexRegister();

    val nodeRun = new NodeRun();
    nodeRun.add(node1);
    nodeRun.add(node2);

    val sortedArray1 = new int[5];
    sortedArray1[0] = 2;
    sortedArray1[1] = 4;
    sortedArray1[2] = 5;
    sortedArray1[3] = 8;
    sortedArray1[4] = 9;

    val sortedArray2 = new int[5];
    sortedArray2[0] = 2;
    sortedArray2[1] = 3;
    sortedArray2[2] = 4;
    sortedArray2[3] = 8;
    sortedArray2[4] = 9;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(node1, new NodeState(ColorRange.RANGE_16BIT, nodeRun, sortedArray1));
    nodeMap.put(node2, new NodeState(ColorRange.RANGE_16BIT, nodeRun, sortedArray2));

    assertEquals(6, execGenerateColorsInRange(2, 10, nodeRun, nodeMap));
  }

  // GENERATE RUN FIRST COLOR

  private static int execGenerateRunFirstColor(NodeRun nodeRun, NodeStatesMap nodeMap) throws Throwable {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateRunFirstColor", NodeRun.class, NodeStatesMap.class);
      m.setAccessible(true);
      return (Integer) m.invoke(null, nodeRun, nodeMap);
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
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, new int[] { 2, 3 }));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 2, 3 }));

    assertEquals(0, execGenerateRunFirstColor(nodeRun, nodeMap));

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, new int[] { 1, 2, 3, 5, 6 }));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, new int[] { 1, 2, 3, 4, 5, 6 }));

    assertEquals(7, execGenerateRunFirstColor(nodeRun, nodeMap));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_Range_0_15() throws Throwable {
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val forbiddenColors = new int[15];
    for (int i = 0; i < forbiddenColors.length; ++i)
      forbiddenColors[i] = i;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_4BIT, nodeRun, forbiddenColors));

    execGenerateRunFirstColor(nodeRun, nodeMap);
  }

  @Test
  public void testGenerateColors_Colorable_RANGE_8BIT() throws Throwable {
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val forbiddenColors1 = new int[249];
    for (int i = 0; i < forbiddenColors1.length; ++i)
      forbiddenColors1[i] = i;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors1));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_8BIT, nodeRun, forbiddenColors1));

    assertEquals(249, execGenerateRunFirstColor(nodeRun, nodeMap));

    val forbiddenColors2 = new int[300];
    forbiddenColors2[0] = 0;
    forbiddenColors2[1] = 1;
    for (int i = 2; i < forbiddenColors2.length; ++i)
      forbiddenColors2[i] = i + 14;

    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors2));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_8BIT, nodeRun, forbiddenColors2));

    assertEquals(2, execGenerateRunFirstColor(nodeRun, nodeMap));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_RANGE_8BIT() throws Throwable {
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val forbiddenColors = new int[255];
    for (int i = 0; i < forbiddenColors.length; ++i)
      forbiddenColors[i] = i;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_8BIT, nodeRun, forbiddenColors));

    execGenerateRunFirstColor(nodeRun, nodeMap);
  }

  @Test
  public void testGenerateColors_Colorable_RANGE_16BIT() throws Throwable {
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val forbiddenColors1 = new int[1000];
    for (int i = 0; i < forbiddenColors1.length; ++i)
      forbiddenColors1[i] = i;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors1));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors1));

    assertEquals(1000, execGenerateRunFirstColor(nodeRun, nodeMap));
  }

  @Test(expected=GraphUncolorableException.class)
  public void testGenerateColors_Uncolorable_RANGE_16BIT() throws Throwable {
    val r = genRegisters(2);
    val nodeRun = new NodeRun(r);

    val forbiddenColors1 = new int[65535];
    for (int i = 0; i < forbiddenColors1.length; ++i)
      forbiddenColors1[i] = i;

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors1));
    nodeMap.put(r[1], new NodeState(ColorRange.RANGE_16BIT, nodeRun, forbiddenColors1));

    execGenerateRunFirstColor(nodeRun, nodeMap);
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

  // CONTAINS ANY OF NODES

  private static boolean execContainsAnyOfNodes(Collection<DexRegister> collection, NodeRun nodeRun) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("containsAnyOfNodes", Collection.class, NodeRun.class);
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
    val r = genRegisters(4);

    val collection = new LinkedList<DexRegister>();
    collection.add(r[0]);
    collection.add(r[1]);

    val run1 = new NodeRun();
    run1.add(r[2]);
    run1.add(r[0]);
    run1.add(r[1]);

    val run2 = new NodeRun();
    run2.add(r[2]);
    run2.add(r[3]);

    assertTrue(execContainsAnyOfNodes(collection, run1));
    assertFalse(execContainsAnyOfNodes(collection, run2));
  }

  // GENERATE CODE WITH SPILLED NODE

  private static DexCode execGenerateCodeWithSpilledNode(DexCode currentCode, NodeRun nodeRun) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateCodeWithSpilledNode", DexCode.class, NodeRun.class);
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

    val nodeRun = new NodeRun();

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

    val nodeRun = new NodeRun();
    nodeRun.add(r2); // new Pair<DexRegister, ColorRange>(r2, ColorRange.RANGE_8BIT));
    nodeRun.add(r3); // new Pair<DexRegister, ColorRange>(r3, ColorRange.RANGE_8BIT));

    val newCode = execGenerateCodeWithSpilledNode(code, nodeRun);
    val insns = newCode.getInstructionList();
    assertTrue(insns.contains(i1));
    assertFalse(insns.contains(i2));
    assertFalse(insns.contains(i3));
    assertTrue(insns.contains(i4));
  }

  // GENERATE UNGAPPED COLORING

  @SuppressWarnings("unchecked")
  private static Pair<Map<DexRegister, Integer>, Integer> execGenerateUngappedColoring(NodeStatesMap nodeMap) {
    try {
      Method m = GraphColoring.class.getDeclaredMethod("generateUngappedColoring", NodeStatesMap.class);
      m.setAccessible(true);
      return (Pair<Map<DexRegister, Integer>, Integer>) m.invoke(null, nodeMap);
    } catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace(System.err);
      fail("Couldn't execute method: " + e.getClass().getSimpleName());
      return null;
    }
  }

  @Test
  public void testGenerateUngappedColoring_Empty() {
    val nodeMap = new NodeStatesMap();
    val coloring = execGenerateUngappedColoring(nodeMap);
    assertEquals(0, (int) coloring.getValB());
    assertTrue(coloring.getValA().isEmpty());
  }

  @Test
  public void testGenerateUngappedColoring_NonEmpty() {
    val r = genRegisters(4);

    val nodeMap = new NodeStatesMap();
    nodeMap.put(r[0], new NodeState(null, null, null, 22));
    nodeMap.put(r[1], new NodeState(null, null, null, 48));
    nodeMap.put(r[2], new NodeState(null, null, null, 48));
    nodeMap.put(r[3], new NodeState(null, null, null, 35));

    val result = execGenerateUngappedColoring(nodeMap);
    assertEquals(3, (int) result.getValB());

    val coloring = result.getValA();
    assertEquals(4, coloring.size());
    assertEquals(0, (int) coloring.get(r[0]));
    assertEquals(2, (int) coloring.get(r[1]));
    assertEquals(2, (int) coloring.get(r[2]));
    assertEquals(1, (int) coloring.get(r[3]));
  }

}
