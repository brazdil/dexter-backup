package uk.ac.cam.db538.dexter.utils;

import lombok.Getter;

public class Triple<A, B, C> {

  @Getter private final A valA;
  @Getter private final B valB;
  @Getter private final C valC;

  public Triple(A valA, B valB, C valC) {
    this.valA = valA;
    this.valB = valB;
    this.valC = valC;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((valA == null) ? 0 : valA.hashCode());
    result = prime * result + ((valB == null) ? 0 : valB.hashCode());
    result = prime * result + ((valC == null) ? 0 : valC.hashCode());
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
    Triple other = (Triple) obj;
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
    if (valC == null) {
      if (other.valC != null)
        return false;
    } else if (!valC.equals(other.valC))
      return false;
    return true;
  }
}
