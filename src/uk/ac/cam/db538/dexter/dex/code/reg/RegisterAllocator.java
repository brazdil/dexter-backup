package uk.ac.cam.db538.dexter.dex.code.reg;

import java.util.List;

public interface RegisterAllocator {
  public RegisterAllocation allocate(List<DexRegister> regs);
}
