package uk.ac.cam.db538.dexter.utils;

import java.util.Collection;
import java.util.LinkedList;

import lombok.val;

public class NoDuplicatesList<E> extends LinkedList<E> {

  private static final long serialVersionUID = -3844134393839278966L;

  private static final RuntimeException DUPLICATE_EXCEPTION = new RuntimeException("Duplicate inserted into NoDuplicatesList");

  @Override
  public boolean add(E e) {
    if (contains(e))
      throw DUPLICATE_EXCEPTION;
    return super.add(e);
  }

  @Override
  public void add(int index, E element) {
    if (contains(element))
      throw DUPLICATE_EXCEPTION;
    super.add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    for (val e : c)
      add(e);
    return true;
  }

  public boolean addAll(E[] c) {
    for (val e : c)
      add(e);
    return true;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    for (val e : c)
      if (contains(e))
        throw DUPLICATE_EXCEPTION;
      else
        super.add(index++, e);
    return true;
  }

  @Override
  public void addFirst(E e) {
    if (contains(e))
      throw DUPLICATE_EXCEPTION;
    super.addFirst(e);
  }

  @Override
  public void addLast(E e) {
    if (contains(e))
      throw DUPLICATE_EXCEPTION;
    super.addLast(e);
  }


}
