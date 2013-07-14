package uk.ac.cam.db538.dexter.dex.code;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class InstructionList implements Collection<DexCodeElement> {

  private final List<DexCodeElement> instructionList;

  public InstructionList(List<? extends DexCodeElement> insns) {
	  this.instructionList = Collections.unmodifiableList(insns); 
  }

  public DexCodeElement peekFirst() {
    if (instructionList.isEmpty())
      return null;
    else
      return instructionList.get(0);
  }

  public DexCodeElement peekLast() {
    if (instructionList.isEmpty())
      return null;
    else
      return instructionList.get(instructionList.size() - 1);
  }

	@Override
	public int size() {
		return instructionList.size();
	}

	@Override
	public boolean isEmpty() {
		return instructionList.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return instructionList.contains(o);
	}
	
	@Override
	public Iterator<DexCodeElement> iterator() {
		return instructionList.iterator();
	}
	
	@Override
	public Object[] toArray() {
		return instructionList.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return instructionList.toArray(a);
	}
	
	@Override
	public boolean add(DexCodeElement e) {
		return instructionList.add(e);
	}
	
	@Override
	public boolean remove(Object o) {
		return instructionList.remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return instructionList.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends DexCodeElement> c) {
		return instructionList.addAll(c);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return instructionList.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return instructionList.retainAll(c);
	}
	
	@Override
	public void clear() {
		instructionList.clear();
	}
	
	private int getIndexOrFail(DexCodeElement elem) {
		int index = instructionList.indexOf(elem);
		if (index < 0)
			throw new NoSuchElementException("Element of InstructionList not found");
		else
			return index;
	}

	public DexCodeElement getFollower(DexCodeElement elem) {
		return instructionList.get(getIndexOrFail(elem) + 1);
	}

	public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, DexCodeElement elemSought) {
		int indexStart = getIndexOrFail(elemStart);
		int indexEnd = getIndexOrFail(elemEnd);
		int indexSougth = getIndexOrFail(elemSought);
		
		return (indexStart <= indexSougth) && (indexSougth <= indexEnd);
	}
}
