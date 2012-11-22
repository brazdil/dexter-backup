package uk.ac.cam.db538.dexter.analysis.coloring;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class NodeRun extends NoDuplicatesList<DexRegister> {
  private static final long serialVersionUID = 5371329873605509798L;

  public NodeRun() {

  }

  public NodeRun(DexRegister[] regs) {
    for (val reg : regs)
      this.add(reg);
  }
}