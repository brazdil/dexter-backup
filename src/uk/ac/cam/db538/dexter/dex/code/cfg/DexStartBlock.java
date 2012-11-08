package uk.ac.cam.db538.dexter.dex.code.cfg;


public class DexStartBlock extends DexBlock {

	@Override
	protected void addPredecessor(DexBlock pred) {
		throw new UnsupportedOperationException("StartBlock cannot have a predecessor");
	}
	
}
