package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22b;
import org.jf.dexlib.Code.Format.Instruction22s;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_BinaryOpLiteral_Test {

  @Test
  public void testParse_BinaryOp() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction22s(Opcode.ADD_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.ADD_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.RSUB_INT, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.RSUB_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.MUL_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.MUL_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.DIV_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.DIV_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.REM_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.REM_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.AND_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.AND_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.OR_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.OR_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22s(Opcode.XOR_INT_LIT16, (byte) 10, (byte) 15, (short) -32000),
        new Instruction22b(Opcode.XOR_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22b(Opcode.SHL_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22b(Opcode.SHR_INT_LIT8, (short) 200, (short) 215, (byte) -120),
        new Instruction22b(Opcode.USHR_INT_LIT8, (short) 200, (short) 215, (byte) -120),
      }, new String[] {
        "add-int/lit v10, v15, #-32000",
        "add-int/lit v200, v215, #-120",
        "rsub-int/lit v10, v15, #-32000",
        "rsub-int/lit v200, v215, #-120",
        "mul-int/lit v10, v15, #-32000",
        "mul-int/lit v200, v215, #-120",
        "div-int/lit v10, v15, #-32000",
        "div-int/lit v200, v215, #-120",
        "rem-int/lit v10, v15, #-32000",
        "rem-int/lit v200, v215, #-120",
        "and-int/lit v10, v15, #-32000",
        "and-int/lit v200, v215, #-120",
        "or-int/lit v10, v15, #-32000",
        "or-int/lit v200, v215, #-120",
        "xor-int/lit v10, v15, #-32000",
        "xor-int/lit v200, v215, #-120",
        "shl-int/lit v200, v215, #-120",
        "shr-int/lit v200, v215, #-120",
        "ushr-int/lit v200, v215, #-120"
      });
  }

  @Test
  public void testAssemble_BinopLit16() {
    val regNTarget = Utils.numFitsInto_Unsigned(4);
    val regNSource = Utils.numFitsInto_Unsigned(4) - 1;
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22s);

    val asmInsn = (Instruction22s) asm[0];
    assertEquals(regNTarget, asmInsn.getRegisterA());
    assertEquals(regNSource, asmInsn.getRegisterB());
    assertEquals(literal, asmInsn.getLiteral());
    assertEquals(Opcode.OR_INT_LIT16, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit16_WrongAllocation_RegTarget() {
    val regNTarget = Utils.numFitsInto_Unsigned(5);
    val regNSource = Utils.numFitsInto_Unsigned(4) - 1;
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit16_WrongAllocation_RegSource() {
    val regNTarget = Utils.numFitsInto_Unsigned(4);
    val regNSource = Utils.numFitsInto_Unsigned(5);
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit16_WrongAllocation_ShlInsn() {
    val regNTarget = Utils.numFitsInto_Unsigned(4);
    val regNSource = Utils.numFitsInto_Unsigned(4) - 1;
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Shl);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit16_WrongAllocation_ShrInsn() {
    val regNTarget = Utils.numFitsInto_Unsigned(4);
    val regNSource = Utils.numFitsInto_Unsigned(4) - 1;
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Shr);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit16_WrongAllocation_UshrInsn() {
    val regNTarget = Utils.numFitsInto_Unsigned(4);
    val regNSource = Utils.numFitsInto_Unsigned(4) - 1;
    val literal = Utils.numFitsInto_Signed(16);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Ushr);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test
  public void testAssemble_BinopLit8() {
    val regNTarget = Utils.numFitsInto_Unsigned(8);
    val regNSource = Utils.numFitsInto_Unsigned(8) - 1;
    val literal = Utils.numFitsInto_Signed(8);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22b);

    val asmInsn = (Instruction22b) asm[0];
    assertEquals(regNTarget, asmInsn.getRegisterA());
    assertEquals(regNSource, asmInsn.getRegisterB());
    assertEquals(literal, asmInsn.getLiteral());
    assertEquals(Opcode.OR_INT_LIT8, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit8_WrongAllocation_RegTarget() {
    val regNTarget = Utils.numFitsInto_Unsigned(9);
    val regNSource = Utils.numFitsInto_Unsigned(8) - 1;
    val literal = Utils.numFitsInto_Signed(8);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_BinopLit8_WrongAllocation_RegSource() {
    val regNTarget = Utils.numFitsInto_Unsigned(8);
    val regNSource = Utils.numFitsInto_Unsigned(9);
    val literal = Utils.numFitsInto_Signed(8);

    val regTarget = new DexRegister(regNTarget);
    val regSource = new DexRegister(regNSource);

    val regAlloc = Utils.genRegAlloc(regTarget, regSource);

    val insn = new DexInstruction_BinaryOpLiteral(null, regTarget, regSource, literal, Opcode_BinaryOpLiteral.Or);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
