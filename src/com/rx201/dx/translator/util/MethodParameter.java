package com.rx201.dx.translator.util;

import org.jf.dexlib.TypeIdItem;

public class MethodParameter {
	private final MethodPrototype method;
	private final int index;
	private final int PrevRegCount;
	private final TypeIdItem type;
	public MethodParameter(MethodPrototype method, int index, int accumulatedRegCount) {
		this.method = method;
		this.index = index;
		this.PrevRegCount = accumulatedRegCount;
		this.type = method.getParameter(index);
	}
	
	public int getRegSize() {
		return type.getRegisterCount();
	}
	
	public TypeIdItem getType() {
		return type;
	}
	
	public int getRelativeRegIndex() {
		return PrevRegCount;
	}
	
	public int getAbsoluteRegIndex() {
		return PrevRegCount + method.getParamRegOffset();
	}
	
	public int getIndex() {
		return index;
	}
}