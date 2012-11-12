package uk.ac.cam.db538.dexter.dex.code.reg;

import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;

public class DexRegister {

  @Getter private final Integer Id;

  public DexRegister() {
    Id = null;
  }

  public DexRegister(Integer id) {
    Id = id;
  }

  public static Cache<Integer, DexRegister> createCache() {
    return new Cache<Integer, DexRegister>() {
      @Override
      protected DexRegister createNewEntry(Integer id) {
        return new DexRegister(id);
      }
    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((Id == null) ? 0 : Id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj); // only take two equal references to be equal
  }
}
