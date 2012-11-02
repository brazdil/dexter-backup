package uk.ac.cam.db538.dexter.dex.code.reg;

import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;

public class DexRegister {

  @Getter private final Integer Id;

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
}
