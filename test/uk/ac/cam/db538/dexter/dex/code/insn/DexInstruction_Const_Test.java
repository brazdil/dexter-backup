package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Const_Test {

  @Test
  public void testParse_Const4() {
    Utils.parseAndCompare(
      new Instruction11n(Opcode.CONST_4, (byte) 13, (byte) 7),
      "const v13, #7");
    Utils.parseAndCompare(
      new Instruction11n(Opcode.CONST_4, (byte) 13, (byte) -8),
      "const v13, #-8");
  }

  @Test
  public void testParse_Const16() {
    Utils.parseAndCompare(
      new Instruction21s(Opcode.CONST_16, (short) 236, (short) 32082),
      "const v236, #32082");
    Utils.parseAndCompare(
      new Instruction21s(Opcode.CONST_16, (short) 236, (short) -32082),
      "const v236, #-32082");
  }

  @Test
  public void testParse_Const() {
    Utils.parseAndCompare(
      new Instruction31i(Opcode.CONST, (short) 237, 0x01ABCDEF),
      "const v237, #28036591");
    Utils.parseAndCompare(
      new Instruction31i(Opcode.CONST, (short) 237, 0xABCDEF01),
      "const v237, #-1412567295");
  }

  @Test
  public void testParse_ConstHigh16() {
    Utils.parseAndCompare(
      new Instruction21h(Opcode.CONST_HIGH16, (short) 238, (short)0x1234),
      "const v238, #305397760");
    Utils.parseAndCompare(
      new Instruction21h(Opcode.CONST_HIGH16, (short) 238, (short)0xABCD),
      "const v238, #-1412628480");
  }

    @Test
  public void testInstrument() {
    val reg1 = new DexRegister(0);
    val reg2 = new DexRegister(1);
    val code = new DexCode();
    code.add(new DexInstruction_Const(code, reg1, 1));
    code.add(new DexInstruction_Const(code, reg2, 0xdec0ded));

    Utils.instrumentAndCompare(
      code,
      new String[] {
        "const v0, #1",
        "const v2, #0",
        "const v1, #233573869",
        "const v3, #1"
      });
  }
}
