package uk.ac.cam.db538.dexter.analysis.coloring;

import java.util.LinkedList;

import lombok.Getter;
import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring.GcColorRange;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.utils.Pair;

public class GraphUncolorableException extends Exception {

  private static final long serialVersionUID = -6108162928298175177L;

  @Getter private final LinkedList<Pair<DexRegister, GcColorRange>> ProblematicNodeRun;

  public GraphUncolorableException(LinkedList<Pair<DexRegister, GcColorRange>> nodeRun) {
    super();
    ProblematicNodeRun = nodeRun;
  }
}
