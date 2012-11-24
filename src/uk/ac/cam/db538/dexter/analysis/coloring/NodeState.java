package uk.ac.cam.db538.dexter.analysis.coloring;

import lombok.Getter;
import lombok.Setter;

class NodeState {
  @Getter private final ColorRange colorRange;
  @Getter private final NodeRun nodeRun;

  @Getter @Setter private Integer color;
  @Getter @Setter private int[] forbiddenColors;

  public NodeState(ColorRange colorRange, NodeRun nodeRun) {
    this.colorRange = colorRange;
    this.nodeRun = nodeRun;
    this.color = null;
    this.forbiddenColors = null;
  }

  public NodeState(ColorRange colorRange, NodeRun nodeRun, int[] forbiddenColors) {
    this(colorRange, nodeRun);
    this.forbiddenColors = forbiddenColors;
  }

  public NodeState(ColorRange colorRange, NodeRun nodeRun, int[] forbiddenColors, int color) {
    this(colorRange, nodeRun, forbiddenColors);
    this.color = color;
  }
}
