package uk.ac.cam.db538.dexter.utils;

import lombok.Getter;
import lombok.NonNull;

public class UnorderedPair<T> {

  @Getter @NonNull private final T valA;
  @Getter @NonNull private final T valB;

  public UnorderedPair(T valA, T valB) {
    this.valA = valA;
    this.valB = valB;

    if (this.valA == null || this.valB == null)
      throw new NullPointerException("UnorderedPair doesn't allow null values");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (valA.hashCode() + valB.hashCode()); // must not depend on the order of valA and valB
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UnorderedPair other = (UnorderedPair) obj;
    if (this.valA.equals(other.valA) && this.valB.equals(other.valB))
      return true;
    else if (this.valA.equals(other.valB) && this.valB.equals(other.valA))
      return true;
    return false;
  }

}
