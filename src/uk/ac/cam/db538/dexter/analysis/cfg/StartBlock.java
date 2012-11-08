package uk.ac.cam.db538.dexter.analysis.cfg;


public class StartBlock extends Block {

  @Override
  protected void addPredecessor(Block pred) {
    throw new UnsupportedOperationException("StartBlock cannot have a predecessor");
  }

}
