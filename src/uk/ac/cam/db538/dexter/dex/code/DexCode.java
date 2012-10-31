package uk.ac.cam.db538.dexter.dex.code;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import lombok.val;

public class DexCode extends LinkedList<DexCodeElement> {

  private static final long serialVersionUID = -4818112329933761251L;

  private int findElement(DexCodeElement elem) {
    int index = 0;
    boolean found = false;
    for (val e : this) {
      if (e.equals(elem)) {
        found = true;
        break;
      }
      index++;
    }

    if (found)
      return index;
    else
      throw new NoSuchElementException();
  }

  public void insertBefore(DexCodeElement elem, DexCodeElement before) {
    this.add(findElement(before), elem);
  }

  public void insertAfter(DexCodeElement elem, DexCodeElement after) {
    this.add(findElement(after) + 1, elem);
  }

//  public void replace(DexCodeElement replaced, DexCodeElement[] replacedWith) {
//	  int location = findElement(replaced);
//	  this.remove(location);
//	  this.addAll(location, Arrays.asList(replacedWith));
//  }
}
