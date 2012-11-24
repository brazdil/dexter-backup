package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;

public class DexRegister {

  @Getter private final Integer id;

  public DexRegister() {
    this.id = null;
  }

  public DexRegister(Integer id) {
    this.id = id;
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj); // only take two equal references to be equal
  }
}
