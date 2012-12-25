package uk.ac.cam.db538.dexter.analysis;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Pair;
import uk.ac.cam.db538.dexter.utils.UnorderedPair;

public class ClashGraph implements Cloneable {

  private static class ClashGraph_Edge extends UnorderedPair<DexRegister> {
    public ClashGraph_Edge(DexRegister rA, DexRegister rB) {
      super(rA, rB);
    }

    public boolean formsEdge(DexRegister reg) {
      return reg.equals(getValA()) || reg.equals(getValB());
    }
  }

  @Getter private final DexCode code;

  private Set<DexRegister> vertices;
  private Set<ClashGraph_Edge> edges;

  public ClashGraph(DexCode code) {
    this.code = code;
    update();
  }

  private ClashGraph(ClashGraph cg) {
    code = cg.code;
    vertices = new HashSet<DexRegister>(cg.vertices);
    edges = new HashSet<ClashGraph_Edge>(cg.edges);
  }

  public void update() {
    vertices = new HashSet<DexRegister>(code.getUsedRegisters());
    edges = new HashSet<ClashGraph_Edge>();

    val LVA = new LiveVarAnalysis(code);
    for (val insn : code.getInstructionList()) {
      // generate all pairs of live-vars-out
      val liveVarsOut_Set = LVA.getLiveVarsOut(insn);
      val liveVarsOut = liveVarsOut_Set.toArray(new DexRegister[liveVarsOut_Set.size()]);
      for (int i = 0; i < liveVarsOut.length - 1; ++i)
        for (int j = i + 1; j < liveVarsOut.length; ++j)
          edges.add(new ClashGraph_Edge(liveVarsOut[i], liveVarsOut[j]));

      // generate pairs from the defined registers to live-vars-in
      val insnDefRegs = insn.lvaDefinedRegisters();
      val liveVarsIn = LVA.getLiveVarsIn(insn);
      for (val defReg : insnDefRegs)
        for (val liveVar : liveVarsIn)
          if (defReg != liveVar)
            edges.add(new ClashGraph_Edge(defReg, liveVar));
    }
  }

  public boolean noVerticesLeft() {
    return vertices.isEmpty();
  }

  public boolean noEdgesLeft() {
    return edges.isEmpty();
  }

  public boolean areClashing(DexRegister rA, DexRegister rB) {
    return edges.contains(new ClashGraph_Edge(rA, rB));
  }

  private int getNodeDegree(DexRegister reg) {
    int degree = 0;
    for (val edge : edges)
      if (edge.formsEdge(reg))
        degree++;
    return degree;
  }

  private void removeNode(DexRegister node) {
    vertices.remove(node);

    val newEdges = new HashSet<ClashGraph_Edge>();
    for (val edge : edges)
      if (!edge.formsEdge(node))
        newEdges.add(edge);
    edges = newEdges;
  }

  public DexRegister removeLowestDegreeNode() {
    Pair<Integer, DexRegister> lowest = null;
    for (val reg : vertices) {
      val degree = getNodeDegree(reg);
      if (lowest == null || lowest.getValA() > degree)
        lowest = new Pair<Integer, DexRegister>(degree, reg);
    }

    if (lowest != null) {
      val reg = lowest.getValB();
      removeNode(reg);
      return reg;
    } else
      return null;
  }

  public Set<DexRegister> getNodeNeighbours(DexRegister node) {
    val neighbours = new HashSet<DexRegister>();

    for (val edge : edges) {
      if (edge.getValA().equals(node))
        neighbours.add(edge.getValB());
      else if (edge.getValB().equals(node))
        neighbours.add(edge.getValA());
    }

    return neighbours;
  }

  @Override
  public Object clone() {
    return new ClashGraph(this);
  }
}
