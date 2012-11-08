package uk.ac.cam.db538.dexter.analysis.cfg;


public class ExitBlock extends Block {

  @Override
  protected void addSuccessor(Block succ) {
    throw new UnsupportedOperationException("ExitBlock cannot have a successor");
  }

}
