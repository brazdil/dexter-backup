package uk.ac.cam.db538.dexter.analysis.coloring;

import lombok.Getter;
import lombok.Setter;

class NodeState {
  @Getter private final ColorRange ColorRange;
  @Getter private final NodeRun NodeRun;

  @Getter @Setter private Integer Color;
  @Getter @Setter private int[] ForbiddenColors;

  public NodeState(ColorRange colorRange, NodeRun nodeRun) {
    ColorRange = colorRange;
    NodeRun = nodeRun;
    Color = null;
    ForbiddenColors = null;
  }

  public NodeState(ColorRange colorRange, NodeRun nodeRun, int[] forbiddenColors) {
    this(colorRange, nodeRun);
    ForbiddenColors = forbiddenColors;
  }

  public NodeState(ColorRange colorRange, NodeRun nodeRun, int[] forbiddenColors, int color) {
    this(colorRange, nodeRun, forbiddenColors);
    Color = color;
  }
}
