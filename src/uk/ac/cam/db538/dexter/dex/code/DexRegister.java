package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.utils.Cache;

public class DexRegister {

  private static int REG_COUNTER = -1;

  private final Integer originalIndex;

  public DexRegister() {
    this.originalIndex = REG_COUNTER;

    REG_COUNTER--;
    if (REG_COUNTER >= 0)
      REG_COUNTER = -1;
  }

  public DexRegister(Integer id) {
    this.originalIndex = id;
  }

  public static Cache<Integer, DexRegister> createCache() {
    return new Cache<Integer, DexRegister>() {
      @Override
      protected DexRegister createNewEntry(Integer id) {
        return new DexRegister(id);
      }
    };
  }

  public int getOriginalIndex() {
    return originalIndex.intValue();
  }

  public String getOriginalIndexString() {
    if (originalIndex == null)
      return "?";
    else
      return originalIndex.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((originalIndex == null) ? 0 : originalIndex.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj); // only take two equal references to be equal
  }
}
