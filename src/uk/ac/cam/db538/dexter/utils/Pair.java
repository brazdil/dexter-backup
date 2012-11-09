package uk.ac.cam.db538.dexter.utils;

import lombok.Getter;
import lombok.NonNull;

public class Pair<A, B> {

  @Getter @NonNull private final A valA;
  @Getter @NonNull private final B valB;

  public Pair(A valA, B valB) {
    this.valA = valA;
    this.valB = valB;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + valA.hashCode();
    result = prime * result + valB.hashCode();
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
    Pair other = (Pair) obj;
    if (!valA.equals(other.valA))
      return false;
    if (!valB.equals(other.valB))
      return false;
    return true;
  }

}
