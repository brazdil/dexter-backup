package uk.ac.cam.db538.dexter.analysis.coloring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.ClashGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.utils.Pair;

public class GraphColoring {

  public static enum GcColorRange {
    Range_0_15,
    Range_0_255,
    Range_0_65535
  }

  private static final int MaxColor = 65536;

  @Getter private final DexCode Code;

  @Getter private DexCode ModifiedCode;
  private Map<DexRegister, Integer> Coloring;
  private int ColorsUsed;

  public GraphColoring(DexCode code) {
    Code = code;
    update();
  }

  public void update() {
    ModifiedCode = Code;
    boolean colored = false;
    while (!colored) {
      try {
        Coloring = generateColoring(
                     new ClashGraph(ModifiedCode),
                     ModifiedCode.getRangeConstraints(),
                     ModifiedCode.getFollowConstraints());
        colored = true;
      } catch (GraphUncolorableException e) {
        if (getStrictestColorRange(e.getProblematicNodeRun()) == GcColorRange.Range_0_65535)
          throw new RuntimeException(e);

        ModifiedCode = generateCodeWithSpilledNode(ModifiedCode, e.getProblematicNodeRun());
      }
    }

    Coloring = removeGapsFromColoring(Coloring);
    val colorsSet = new HashSet<Integer>(Coloring.values());
    ColorsUsed = colorsSet.size();
  }

  private static Map<DexRegister, Integer> generateColoring(ClashGraph clashGraph, Map<DexRegister, GcColorRange> nodeRanges, Set<LinkedList<DexRegister>> nodeFollowUps) throws GraphUncolorableException {
    // must not modify the clashGraph !

    val coloring = new HashMap<DexRegister, Integer>();

    val nodeStack = createNodeStack(clashGraph);
    while (!nodeStack.isEmpty()) {
      val node = nodeStack.pop();
      val nodeFollowUpRun = getNodeFollowUpRun(node, nodeFollowUps, nodeRanges);
      val forbiddenColors = getColorsUsedByRunNeighbours(nodeFollowUpRun, coloring, clashGraph);

      val color = generateColors(nodeFollowUpRun, forbiddenColors);
      coloring.put(node, Integer.valueOf(color));

      // remove all nodes from the same run from the stack
      for (val nodeInRun : nodeFollowUpRun)
        nodeStack.remove(nodeInRun.getValA());
    }

    return coloring;
  }

  private static Stack<DexRegister> createNodeStack(ClashGraph cg) {
    // must create a clone of the clash graph
    val cgClone = (ClashGraph) cg.clone();
    val stack = new Stack<DexRegister>();
    while (!cgClone.noVerticesLeft())
      stack.push(cgClone.removeLowestDegreeNode());
    return stack;
  }

  private static LinkedList<Pair<DexRegister, GcColorRange>> getNodeFollowUpRun(DexRegister node, Set<LinkedList<DexRegister>> nodeFollowUps,  Map<DexRegister, GcColorRange> nodeRanges) {
    for (val run : nodeFollowUps)
      if (run.contains(node)) {
        val runWithRanges = new LinkedList<Pair<DexRegister, GcColorRange>>();
        for (val nodeInRun : run) {
          val range = nodeRanges.get(nodeInRun);
          runWithRanges.add(new Pair<DexRegister, GraphColoring.GcColorRange>(nodeInRun, (range == null) ? GcColorRange.Range_0_65535 : range));
        }
        return runWithRanges;
      }

    // should never happen
    throw new RuntimeException("Couldn't find a run containing the node");
  }

  private static GcColorRange getStrictestColorRange(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    GcColorRange strictest = GcColorRange.Range_0_65535;

    for (val nodePair : nodeRun) {
      val nodeRange = nodePair.getValB();
      if (nodeRange.ordinal() < strictest.ordinal())
        strictest = nodeRange;
    }

    return strictest;
  }

  private static Set<Integer> getColorsUsedByRunNeighbours(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, Map<DexRegister, Integer> coloringState, ClashGraph clashGraph) {
    val colors = new HashSet<Integer>();

    for (val node : nodeRun) {
      for (val neighbour : clashGraph.getNodeNeighbours(node.getValA())) {
        val neighbourColor = coloringState.get(neighbour);
        if (neighbourColor != null)
          colors.add(neighbourColor);
      }
    }

    return colors;
  }

  private static int generateColors(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, Set<Integer> forbiddenColors) throws GraphUncolorableException {
    val strictestRunRange = getStrictestColorRange(nodeRun);
    val sortedForbiddenColors = new ArrayList<Integer>(forbiddenColors);
    Collections.sort(sortedForbiddenColors);

    // start looking for the colors in the strictest range enforced by the nodes
    // if that fails, try extending it to even stricter ranges
    switch (strictestRunRange) {
    case Range_0_15:
      return generateColorsInRange(0, 15, nodeRun, sortedForbiddenColors);
    case Range_0_255:
      try {
        return generateColorsInRange(16, 255, nodeRun, sortedForbiddenColors);
      } catch (GraphUncolorableException e) {
        return generateColorsInRange(0, 255, nodeRun, sortedForbiddenColors);
      }
    case Range_0_65535:
    default:
      try {
        return generateColorsInRange(256, 65535, nodeRun, sortedForbiddenColors);
      } catch (GraphUncolorableException e) {
        return generateColorsInRange(0, 65535, nodeRun, sortedForbiddenColors);
      }
    }
  }

  private static int generateColorsInRange(int low, int high, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun, ArrayList<Integer> sortedForbiddenColors) throws GraphUncolorableException {
    while (true) {
      // find a starting position, i.e. color that is inside the range
      // and there is enough space before the next color for the whole run

      val color = firstUnusedHigherOrEqualColor(low, sortedForbiddenColors);
      if (color == -1 || color > high)
        throw new GraphUncolorableException(nodeRun);
      val nextUsedColor = nextUsedHigherColor(color, sortedForbiddenColors);

      if (nextUsedColor - color < nodeRun.size()) {
        low = nextUsedColor;
        continue;
      }

      // now check that the constraints are satisfied
      int i = 0;
      for (val node : nodeRun) {
        val nodeColor = color + i;
        val nodeRange = node.getValB();

        if ((nodeRange == GcColorRange.Range_0_15 && nodeColor > 15) ||
            (nodeRange == GcColorRange.Range_0_255 && nodeColor > 255) ||
            (nodeRange == GcColorRange.Range_0_65535 && nodeColor > 65535)) // last case should never happen, but just in case...
          throw new GraphUncolorableException(nodeRun);

        i++;
      }

      // if they are, return the first color
      return color;
    }
  }

  private static int firstUnusedHigherOrEqualColor(int color, ArrayList<Integer> sortedColors) {
    while (sortedColors.contains(color))
      color++;

    if (color >= MaxColor)
      return -1;
    else
      return color;
  }

  private static int nextUsedHigherColor(int color, ArrayList<Integer> sortedColors) {
    for (val arrayColor : sortedColors)
      if (arrayColor > color)
        return arrayColor > MaxColor ? MaxColor : arrayColor;
    return MaxColor;
  }

  private static boolean containsAnyOfNodes(Collection<DexRegister> collection, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    for (val node : nodeRun)
      if (collection.contains(node.getValA()))
        return true;
    return false;
  }

  private static DexCode generateCodeWithSpilledNode(DexCode currentCode, LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    val newCode = new DexCode(null);

    val spilledRegs = new HashSet<DexRegister>();
    for (val node : nodeRun)
      spilledRegs.add(node.getValA());

    for (val insn : currentCode.getInstructionList()) {
      if (containsAnyOfNodes(insn.lvaReferencedRegisters(), nodeRun) || containsAnyOfNodes(insn.lvaDefinedRegisters(), nodeRun))
        newCode.addAll(insn.gcAddTemporaries(spilledRegs));
      else
        newCode.add(insn);
    }
    return newCode;
  }

  private static Map<DexRegister, Integer> removeGapsFromColoring(Map<DexRegister, Integer> oldColoring) {
    val newColoring = new HashMap<DexRegister, Integer>();

    // create set of all allocated colors
    val allocatedColors = new HashSet<Integer>();
    for (val color : oldColoring.values())
      allocatedColors.add(color);

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
    for (val entry : oldColoring.entrySet())
      newColoring.put(entry.getKey(), ungappedColors.get(entry.getValue()));

    return newColoring;
  }

  public int getColor(DexRegister node) {
    val color = Coloring.get(node);
    if (color != null)
      return color;
    else
      throw new RuntimeException("Asked for coloring of an unexistent node");
  }

  public int getNumberOfColorsUsed() {
    return ColorsUsed;
  }
}
