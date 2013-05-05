package com.rx201.dx.translator.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;


public class MethodPrototype implements Iterable<MethodParameter> {
	private final MethodIdItem method;
	private final Boolean hasThisPtr;
	private final TypeIdItem thisPtrType;
	private final int paramRegOffset;
	
	public MethodPrototype(CodeItem codeItem) {
		EncodedMethod encMethod = codeItem.getParent();
		method = encMethod.method;
		
		if ((encMethod.accessFlags & AccessFlags.STATIC.getValue()) == 0) {
			hasThisPtr = true;
			thisPtrType = method.getContainingClass();
		} else {
			hasThisPtr = false;
			thisPtrType = null;
		}
		paramRegOffset = codeItem.getRegisterCount() - getRegisterCount();
	}

	public MethodPrototype(MethodIdItem method, boolean isStatic) {
		this.method = method;
		if (isStatic) {
			hasThisPtr = false;
			thisPtrType = null;
		} else {
			hasThisPtr = true;
			thisPtrType = method.getContainingClass();
		}
		paramRegOffset = -1;
	}
	
	public TypeIdItem getParameter(int index) {
		if (hasThisPtr) {
			if (index == 0)
				return thisPtrType;
			else
				return method.getPrototype().getParameters().getTypeIdItem(index - 1);
		} else
			return method.getPrototype().getParameters().getTypeIdItem(index);
	}
	
	public int getParameterCount() {
		TypeListItem params = method.getPrototype().getParameters();
		int count = (params != null) ? params.getTypeCount() : 0;
		if (hasThisPtr)
			count++;
		return count;
	}
	
	public int getRegisterCount() {
		int count = method.getPrototype().getParameterRegisterCount();
		if (hasThisPtr)
			count++;
		return count;
	}

	public int getParamRegOffset() {
		if (paramRegOffset < 0)
			throw new RuntimeException("MethodPrototype created from MethodIdItem does not support register offset lookup.");
		return paramRegOffset;
	}
	
	@Override
	public Iterator<MethodParameter> iterator() {
		return new Iterator<MethodParameter>() {
			private int index = 0;
			private int accumulatedRegCount = 0;
			@Override
			public boolean hasNext() {
				return index < MethodPrototype.this.getParameterCount();
			}

			@Override
			public MethodParameter next() {
				if (!hasNext())
					throw new NoSuchElementException();
				MethodParameter p = new MethodParameter(MethodPrototype.this, index, accumulatedRegCount);
				accumulatedRegCount += p.getRegSize();
				index++;
				return p;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
}
