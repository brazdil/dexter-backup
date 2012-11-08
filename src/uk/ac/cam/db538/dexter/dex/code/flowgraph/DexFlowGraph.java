package uk.ac.cam.db538.dexter.dex.code.flowgraph;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexFlowGraph {

  @Getter private final DexStartBlock StartBlock;

  public DexFlowGraph(DexCode code) {

    StartBlock = new DexStartBlock();

  }
}
