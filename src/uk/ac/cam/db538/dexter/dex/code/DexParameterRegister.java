package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexParameterRegister extends DexRegister {

  @Getter private final int parameterIndex;

  public DexParameterRegister(int paramIndex) {
    this.parameterIndex = paramIndex;
  }

  @Override
  public String getOriginalIndexString() {
    return "p" + parameterIndex;
  }

}
