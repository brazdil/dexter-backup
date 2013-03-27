package com.rx201.dx.translator;

import com.android.dx.rop.code.BasicBlockList;
import com.android.dx.rop.code.RopMethod;
import com.android.dx.util.IntList;

public class SimpleRopMethod extends RopMethod {

	public SimpleRopMethod(BasicBlockList blocks, int firstLabel) {
		super(blocks, firstLabel);
	}

	@Override
	public BasicBlockList getBlocks() {
		return super.getBlocks();
	}

	@Override
	public int getFirstLabel() {
		return super.getFirstLabel();
	}

	@Override
	public IntList labelToPredecessors(int label) {
		return super.labelToPredecessors(label);
	}

	@Override
	public IntList getExitPredecessors() {
		throw new UnsupportedOperationException();
//		return super.getExitPredecessors();
	}

	@Override
	public RopMethod withRegisterOffset(int delta) {
		throw new UnsupportedOperationException();
//		return super.withRegisterOffset(delta);
	}
	
}
