package uk.ac.cam.db538.dexter.analysis.coloring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.ClashGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.Pair;

public class GraphColoring {

  private static final int MAX_COLOR = 65536;
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  @Getter private final DexCode code;

  private Map<DexRegister, Integer> coloring;
  @Getter private int colorsUsed;

  public GraphColoring(DexCode code) {
    this.code = code;
    generateGC();
  }

  private void generateGC() {
    boolean colored = false;
    while (!colored) {
      val nodeMap = generateNodeStates(code);
      val clashGraph = new ClashGraph(code);
      try {
        colorGraph(nodeMap, clashGraph);
        val result = generateUngappedColoring(nodeMap);

        coloring = result.getValA();
        colorsUsed = result.getValB();

        colored = true;
      } catch (GraphUncolorableException e) {
        val strictestColorRange = getStrictestColorRange(e.getProblematicNodeRun(), nodeMap);
        if (strictestColorRange == ColorRange.RANGE_16BIT)
          throw new RuntimeException(e);

        val similarNodeRuns = new ArrayList<NodeRun>();
        for (val nodeState : nodeMap.values()) {
          val nodeRun = nodeState.getNodeRun();
          if (getStrictestColorRange(nodeRun, nodeMap).ordinal() <= strictestColorRange.ordinal())
            similarNodeRuns.add(nodeRun);
        }

        boolean spilled = false;
        while (!spilled) {
          val originalInstructionList = new ArrayList<DexCodeElement>(code.getInstructionList());
          try {
            int randomInt = RANDOM.nextInt(similarNodeRuns.size());
            val randomNodeRun = similarNodeRuns.get(randomInt);
            spillNodeInCode(code, randomNodeRun);
            spilled = true;
          } catch (UnsupportedOperationException ex) {
            // revert changes
            code.replaceInstructions(originalInstructionList);
          }
        }
      }
    }
  }

  private static NodeStatesMap generateNodeStates(DexCode code) {
    val nodeMap = new NodeStatesMap();

    val registers = code.getUsedRegisters();
    val rangeConstraints = code.getRangeConstraints();
    val followRuns = code.getFollowRuns();

    for (val node : registers) {
      ColorRange range = rangeConstraints.get(node);
      if (range == null)
        range = ColorRange.RANGE_16BIT;
      val nodeRun = followRuns.get(node);

      nodeMap.put(node, new NodeState(range, nodeRun));
    }

    return nodeMap;
  }

  private static void colorGraph(NodeStatesMap nodeMap, ClashGraph clashGraph) throws GraphUncolorableException {
    // must not modify the clashGraph !

    val nodeStack = createNodeStack(clashGraph);

    while (!nodeStack.isEmpty()) {
      val nodeRun = nodeMap.get(nodeStack.pop()).getNodeRun();
      processRun(nodeRun, nodeMap, clashGraph, nodeStack);
    }
  }

  private static Stack<DexRegister> createNodeStack(ClashGraph cg) {
    // must create a clone of the clash graph
    val cgClone = (ClashGraph) cg.clone();
    val stack = new Stack<DexRegister>();
    while (!cgClone.noVerticesLeft())
      stack.push(cgClone.removeLowestDegreeNode());
    return stack;
  }

  private static void processRun(NodeRun nodeRun, NodeStatesMap nodeMap, ClashGraph clashGraph, Stack<DexRegister> nodeStack) throws GraphUncolorableException {
    generateRunForbiddenColors(nodeRun, nodeMap, clashGraph);
    generateRunColoring(nodeRun, nodeMap);
    removeRunFromStack(nodeRun, nodeMap, nodeStack);
  }

  private static void generateRunForbiddenColors(NodeRun nodeRun, NodeStatesMap nodeMap, ClashGraph clashGraph) {
    // for each of the nodes in the node's run
    for (val node : nodeRun.getNodes()) {
      // find the union of colors assigned to its neighbours
      val nodeColors = new HashSet<Integer>();
      for (val neighbour : clashGraph.getNodeNeighbours(node)) {
        val neighbourColor = nodeMap.get(neighbour).getColor();
        if (neighbourColor != null)
          nodeColors.add(neighbourColor);
      }

      // turn it into a sorted array
      val nodeColorsArray = new int[nodeColors.size()];
      int i = 0;
      for (val color : nodeColors)
        nodeColorsArray[i++] = color;
      Arrays.sort(nodeColorsArray);

      // store in the map with the node
      nodeMap.get(node).setForbiddenColors(nodeColorsArray);
    }
  }

  private static void generateRunColoring(NodeRun nodeRun, NodeStatesMap nodeMap) throws GraphUncolorableException {
    int color = generateRunFirstColor(nodeRun, nodeMap);
    for (val node : nodeRun.getNodes())
      nodeMap.get(node).setColor(color++);
  }

  private static void removeRunFromStack(NodeRun nodeRun, NodeStatesMap nodeMap, Stack<DexRegister> nodeStack) {
    for (val node : nodeRun.getNodes())
      nodeStack.remove(node);
  }

  private static ColorRange getStrictestColorRange(NodeRun nodeRun, NodeStatesMap nodeMap) {
    ColorRange strictest = ColorRange.RANGE_16BIT;
    for (val node : nodeRun.getNodes()) {
      val nodeRange = nodeMap.get(node).getColorRange();
      if (nodeRange.ordinal() < strictest.ordinal())
        strictest = nodeRange;
    }

    return strictest;
  }

  private static int generateRunFirstColor(NodeRun nodeRun, NodeStatesMap nodeMap) throws GraphUncolorableException {
    // start looking for the colors in the strictest range enforced by the nodes
    // if that fails, try extending it to even stricter ranges
    switch (getStrictestColorRange(nodeRun, nodeMap)) {
    case RANGE_4BIT:
      return generateColorsInRange(0, 15, nodeRun, nodeMap);
    case RANGE_8BIT:
      try {
        return generateColorsInRange(16, 255, nodeRun, nodeMap);
      } catch (GraphUncolorableException e) {
        return generateColorsInRange(0, 255, nodeRun, nodeMap);
      }
    case RANGE_16BIT:
    default:
      try {
        return generateColorsInRange(256, 65535, nodeRun, nodeMap);
      } catch (GraphUncolorableException e) {
        return generateColorsInRange(0, 65535, nodeRun, nodeMap);
      }
    }
  }

  private static int generateColorsInRange(int low, int high, NodeRun nodeRun, NodeStatesMap nodeMap) throws GraphUncolorableException {
    val firstNode = nodeRun.peekFirst();
    val firstNodeForbiddenColors = nodeMap.get(firstNode).getForbiddenColors();

    while (true) {
      // find a starting position, i.e. color assignable to the first node
      // of the run, such that all the others are assignable too

      val firstColor = firstUnusedHigherOrEqualColor(low, firstNodeForbiddenColors);
      if (firstColor == -1 || firstColor > high) // -1 when MaxColor is exceeded
        throw new GraphUncolorableException(nodeRun);

      // next call will return true if coloring is valid,
      // false if it is not, and throw an exception if
      // the color numbers are so high it exceeds some
      // of the range constraints; in that case, the graph
      // is not colorable
      if (!checkColorRange(firstColor, nodeRun, nodeMap)) {
        low = nextUsedHigherColor(firstColor, firstNodeForbiddenColors);
        continue;
      }

      return firstColor;
    }
  }

  private static boolean checkColorRange(int firstColor, NodeRun nodeRun, NodeStatesMap nodeMap) throws GraphUncolorableException {
    int currentColor = firstColor;

    for (val node : nodeRun.getNodes()) {
      val nodeEntry = nodeMap.get(node);
      val nodeForbiddenColors = nodeEntry.getForbiddenColors();
      val nodeRange = nodeEntry.getColorRange();

      if (Arrays.binarySearch(nodeForbiddenColors, currentColor) >= 0) // if currentColor is in the array
        return false;
      else if (!nodeRange.isInRange(currentColor))
        throw new GraphUncolorableException(nodeRun);
      else
        currentColor++;
    }

    return true;
  }

  private static int firstUnusedHigherOrEqualColor(int color, int[] sortedColors) {
    while (Arrays.binarySearch(sortedColors, color) >= 0)
      color++;

    if (color >= MAX_COLOR)
      return -1;
    else
      return color;
  }

  private static int nextUsedHigherColor(int color, int[] sortedColors) {
    for (val arrayColor : sortedColors)
      if (arrayColor > color)
        return arrayColor > MAX_COLOR ? MAX_COLOR : arrayColor;
    return MAX_COLOR;
  }

  private static boolean containsAnyOfNodes(Collection<DexRegister> collection, NodeRun nodeRun) {
    for (val node : nodeRun.getNodes())
      if (collection.contains(node))
        return true;
    return false;
  }

  private static void spillNodeInCode(DexCode code, NodeRun nodeRun) {
    val replacedInstructions = new HashSet<DexCodeElement>();
    for (val insn : code.getInstructionList()) {
      if (containsAnyOfNodes(insn.lvaUsedRegisters(), nodeRun))
        replacedInstructions.add(insn);
    }

    for (val insn : replacedInstructions) {
      val replacementMapping = insn.gcAddTemporaries(nodeRun.getNodes());
      for (val entry : replacementMapping.entrySet())
        code.replace(entry.getKey(), entry.getValue());
    }
  }

  private static Pair<Map<DexRegister, Integer>, Integer> generateUngappedColoring(NodeStatesMap nodeMap) {
    // create set of all allocated colors
    val allocatedColors = new HashSet<Integer>();
    for (val nodeInfo : nodeMap.values())
      allocatedColors.add(nodeInfo.getColor());

    // create mapping from gapped allocation to ungapped
    val ungappedColors = new HashMap<Integer, Integer>();
    for (val color : allocatedColors) {
      int smallerColors = 0;
      for (val color2 : allocatedColors)
        if (color2 < color)
          smallerColors++;
      ungappedColors.put(color, smallerColors);
    }

    // create new coloring with this new allocation
    val coloring = new HashMap<DexRegister, Integer>();
    for (val entry : nodeMap.entrySet())
      coloring.put(entry.getKey(), ungappedColors.get(entry.getValue().getColor()));

    // store the number of colors
    val colorsUsed = ungappedColors.size();

    return new Pair<Map<DexRegister, Integer>, Integer>(coloring, colorsUsed);
  }

  public Map<DexRegister, Integer> getColoring() {
    return Collections.unmodifiableMap(coloring);
  }

  public int getColor(DexRegister node) {
    val color = coloring.get(node);
    if (color != null)
      return color;
    else
      throw new RuntimeException("Asked for coloring of an unexistent node");
  }
}
