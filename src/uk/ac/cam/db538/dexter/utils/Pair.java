package uk.ac.cam.db538.dexter.utils;

import lombok.Getter;

public class Pair<A, B> {

  @Getter private final A valA;
  @Getter private final B valB;

  public Pair(A valA, B valB) {
    this.valA = valA;
    this.valB = valB;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((valA == null) ? 0 : valA.hashCode());
    result = prime * result + ((valB == null) ? 0 : valB.hashCode());
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
    if (valA == null) {
      if (other.valA != null)
        return false;
    } else if (!valA.equals(other.valA))
      return false;
    if (valB == null) {
      if (other.valB != null)
        return false;
    } else if (!valB.equals(other.valB))
      return false;
    return true;
  }

}
