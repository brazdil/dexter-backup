package uk.ac.cam.db538.dexter.dex.code.cfg;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexFlowGraph {

  @Getter private final DexStartBlock StartBlock;
  @Getter private final DexExitBlock ExitBlock;

  public DexFlowGraph(DexCode code) {

    StartBlock = new DexStartBlock();
    ExitBlock = new DexExitBlock();

  }
}
