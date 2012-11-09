package uk.ac.cam.db538.dexter.dex.code.reg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import lombok.val;

import org.junit.Test;

public class RegisterAllocator_Append_Test {

  @Test
  public void testAllocate_AllIds() {
    val reg0 = new DexRegister(1);
    val reg1 = new DexRegister(2);
    val reg2 = new DexRegister(8);
    val reg3 = new DexRegister(340);

    val regs = new HashSet<DexRegister>();
    regs.add(reg1);
    regs.add(reg3);
    regs.add(reg2);
    regs.add(reg0);

    // allocation should sort them
    val allocation = (new RegisterAllocator_Append()).allocate(regs);

    assertEquals(Integer.valueOf(0), allocation.get(reg0));
    assertEquals(Integer.valueOf(1), allocation.get(reg1));
    assertEquals(Integer.valueOf(2), allocation.get(reg2));
    assertEquals(Integer.valueOf(3), allocation.get(reg3));
  }

  @Test
  public void testAllocate_OneIdMissing() {
    val reg0 = new DexRegister(0);
    val reg1 = new DexRegister(1);
    val reg2 = new DexRegister(2);
    val reg3 = new DexRegister(null);

    val regs = new HashSet<DexRegister>();
    regs.add(reg1);
    regs.add(reg3);
    regs.add(reg2);
    regs.add(reg0);

    // allocation should put the null one at the end
    val allocation = (new RegisterAllocator_Append()).allocate(regs);

    assertEquals(Integer.valueOf(0), allocation.get(reg0));
    assertEquals(Integer.valueOf(1), allocation.get(reg1));
    assertEquals(Integer.valueOf(2), allocation.get(reg2));
    assertEquals(Integer.valueOf(3), allocation.get(reg3));
  }

  @Test
  public void testAllocate_MoreIdsMissing() {
    val reg0 = new DexRegister(0);
    val reg1 = new DexRegister(1);
    val reg2 = new DexRegister(null);
    val reg3 = new DexRegister(null);

    val regs = new HashSet<DexRegister>();
    regs.add(reg1);
    regs.add(reg3);
    regs.add(reg2);
    regs.add(reg0);

    // allocation should put the null ones at the end
    // in that case, the order doesn't matter
    val allocation = (new RegisterAllocator_Append()).allocate(regs);

    assertEquals(Integer.valueOf(0), allocation.get(reg0));
    assertEquals(Integer.valueOf(1), allocation.get(reg1));
    assertTrue((allocation.get(reg2) == 2 && allocation.get(reg3) == 3) ||
               (allocation.get(reg2) == 3 && allocation.get(reg3) == 2));
  }
}
