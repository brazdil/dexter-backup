package uk.ac.cam.db538.dexter.analysis.cfg;


public class CfgStartBlock extends CfgBlock {

  @Override
  protected void addPredecessor(CfgBlock pred) {
    throw new UnsupportedOperationException("StartBlock cannot have a predecessor");
  }
}
