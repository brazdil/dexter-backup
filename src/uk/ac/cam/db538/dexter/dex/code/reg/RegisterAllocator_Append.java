package uk.ac.cam.db538.dexter.dex.code.reg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import lombok.val;

public class RegisterAllocator_Append implements RegisterAllocator {

  @Override
  public RegisterAllocation allocate(Set<DexRegister> regSet) {
    val regs = new ArrayList<DexRegister>(regSet);

    Collections.sort(regs, new Comparator<DexRegister>() {
      @Override
      public int compare(DexRegister o1, DexRegister o2) {
        if (o1.getId() != null && o2.getId() != null)
          return o1.getId().compareTo(o2.getId());
        else if (o1.getId() != null)
          return -1;
        else if (o2.getId() != null)
          return 1;
        else
          return 0;
      }
    });

    int counter = 0;
    val allocation = new RegisterAllocation();
    for (val reg : regs)
      allocation.put(reg, counter++);

    return allocation;
  }

}
