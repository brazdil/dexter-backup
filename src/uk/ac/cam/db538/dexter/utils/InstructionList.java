package uk.ac.cam.db538.dexter.utils;

import java.util.ArrayList;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class InstructionList extends ArrayList<DexCodeElement> {

  private static final long serialVersionUID = -3844134393839278966L;

  public InstructionList() {
    super();
  }

  public InstructionList(int initialCapacity) {
    super(initialCapacity);
  }

  public boolean addAll(DexCodeElement[] c) {
    for (val e : c)
      add(e);
    return true;
  }

  public DexCodeElement peekFirst() {
    if (isEmpty())
      return null;
    else
      return get(0);
  }

  public DexCodeElement peekLast() {
    if (isEmpty())
      return null;
    else
      return get(size() - 1);
  }
}
