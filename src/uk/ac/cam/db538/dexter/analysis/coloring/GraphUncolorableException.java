package uk.ac.cam.db538.dexter.analysis.coloring;

import lombok.Getter;

public class GraphUncolorableException extends Exception {

  private static final long serialVersionUID = -6108162928298175177L;

  @Getter private final NodeRun problematicNodeRun;

  public GraphUncolorableException(NodeRun nodeRun) {
    super();
    problematicNodeRun = nodeRun;
  }
}
