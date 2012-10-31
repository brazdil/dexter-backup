package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;
import lombok.Setter;

public class DexRegister {

  @Getter @Setter private int Id;

  public DexRegister(int id) {
    Id = id;
  }
}
