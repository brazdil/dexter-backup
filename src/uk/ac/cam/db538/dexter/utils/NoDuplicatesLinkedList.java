package uk.ac.cam.db538.dexter.utils;

import java.util.Collection;
import java.util.LinkedList;

import lombok.val;

public class NoDuplicatesLinkedList<E> extends LinkedList<E> {

  private static final long serialVersionUID = -3844134393839278966L;

  @Override
  public boolean add(E e) {
    if (!contains(e))
      return super.add(e);
    return true;
  }

  @Override
  public void add(int index, E element) {
    if (!contains(element))
      super.add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    for (val e : c)
      add(e);
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    for (val e : c)
      if (!contains(e))
        super.add(index++, e);
    return true;
  }

  @Override
  public void addFirst(E e) {
    if (!contains(e))
      super.addFirst(e);
  }

  @Override
  public void addLast(E e) {
    if (!contains(e))
      super.addLast(e);
  }


}
