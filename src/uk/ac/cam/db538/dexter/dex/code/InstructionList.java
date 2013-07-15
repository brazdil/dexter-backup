package uk.ac.cam.db538.dexter.dex.code;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;

public class InstructionList implements Collection<DexCodeElement> {

  private final List<DexCodeElement> instructionList;

  public InstructionList(List<? extends DexCodeElement> insns) {
	  // check instruction list for duplicates
	  // (often need to find the index of an instruction,
	  //  so having duplicates could result in finding
	  //  the wrong occurence)
	  val visited = new HashSet<DexCodeElement>();
	  for (val insn : insns)
		  if (visited.contains(insn))
			  throw new IllegalArgumentException("Duplicates are not allowed in the instruction list");
		  else
			  visited.add(insn);
	  
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
	
	public int getIndex(DexCodeElement elem) {
		int index = instructionList.indexOf(elem);
		if (index < 0)
			throw new NoSuchElementException("Element of InstructionList not found");
		else
			return index;
	}

	public DexCodeElement getFollower(DexCodeElement elem) {
		return instructionList.get(getIndex(elem) + 1);
	}

	public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, int indexSought) {
		int indexStart = getIndex(elemStart);
		int indexEnd = getIndex(elemEnd);
		
		return (indexStart <= indexSought) && (indexSought <= indexEnd);
	}

	public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, DexCodeElement elemSought) {
		return isBetween(elemStart, elemEnd, getIndex(elemSought));
	}
	
	public List<DexTryStart> getAllTryBlocks() {
		val list = new ArrayList<DexTryStart>();
		for (val insn : instructionList)
			if (insn instanceof DexTryStart)
				list.add((DexTryStart) insn);
		return list;
	}
}
