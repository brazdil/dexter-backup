package uk.ac.cam.db538.dexter.dex.code.reg;

import java.util.Set;

public interface RegisterAllocator {
  public RegisterAllocation allocate(Set<DexRegister> regs);
}
