package uk.ac.cam.db538.dexter.analysis.cfg;


public class CfgExitBlock extends CfgBlock {

  @Override
  protected void addSuccessor(CfgBlock succ) {
    throw new UnsupportedOperationException("ExitBlock cannot have a successor");
  }

}
