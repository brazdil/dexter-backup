package uk.ac.cam.db538.dexter.dex.code.cfg;


public class DexExitBlock extends DexBlock {

	@Override
	protected void addSuccessor(DexBlock succ) {
		throw new UnsupportedOperationException("ExitBlock cannot have a successor");
	}
	
}
