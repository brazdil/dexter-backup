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

  @Getter private final DexCode Code;

  private Set<DexRegister> Vertices;
  private Set<ClashGraph_Edge> Edges;

  public ClashGraph(DexCode code) {
    Code = code;

    update();
  }

  private ClashGraph(ClashGraph cg) {
    Code = cg.Code;
    Vertices = new HashSet<DexRegister>(cg.Vertices);
    Edges = new HashSet<ClashGraph_Edge>(cg.Edges);
  }

  public void update() {
    Vertices = new HashSet<DexRegister>(Code.getUsedRegisters());
    Edges = new HashSet<ClashGraph_Edge>();

    val LVA = new LiveVarAnalysis(Code);
    for (val insn : Code.getInstructionList()) {
      val liveVars = LVA.getLiveVarsAt(insn).toArray(new DexRegister[0]);

      // generate all pairs
      for (int i = 0; i < liveVars.length - 1; ++i)
        for (int j = i + 1; j < liveVars.length; ++j)
          Edges.add(new ClashGraph_Edge(liveVars[i], liveVars[j]));
    }
  }

  public boolean noVerticesLeft() {
    return Vertices.isEmpty();
  }

  public boolean noEdgesLeft() {
    return Edges.isEmpty();
  }

  public boolean areClashing(DexRegister rA, DexRegister rB) {
    return Edges.contains(new ClashGraph_Edge(rA, rB));
  }

  private int getNodeDegree(DexRegister reg) {
    int degree = 0;
    for (val edge : Edges)
      if (edge.formsEdge(reg))
        degree++;
    return degree;
  }

  private void removeNode(DexRegister node) {
    Vertices.remove(node);

    val newEdges = new HashSet<ClashGraph_Edge>();
    for (val edge : Edges)
      if (!edge.formsEdge(node))
        newEdges.add(edge);
    Edges = newEdges;
  }

  public DexRegister removeLowestDegreeNode() {
    Pair<Integer, DexRegister> lowest = null;
    for (val reg : Vertices) {
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

    for (val edge : Edges) {
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
