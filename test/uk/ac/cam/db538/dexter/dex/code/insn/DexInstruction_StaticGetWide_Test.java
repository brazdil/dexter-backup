package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.*;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexInstruction_StaticGetWide_Test {

  @Test
  public void testParse_StaticGetWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.SGET_WIDE, (short) 240, Utils.getFieldItem("Lcom/example/MyClass5;", "J", "TestField5")),
        new Instruction21c(Opcode.SGET_WIDE, (short) 241, Utils.getFieldItem("Lcom/example/MyClass6;", "D", "TestField6"))
      }, new String[] {
        "sget-wide v240, com.example.MyClass5.TestField5",
        "sget-wide v241, com.example.MyClass6.TestField6",
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGetWide_WrongType() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_WIDE, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test
  public void testAssemble_StaticGetWide() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_StaticGetWide(
      null,
      regTo1,
      regTo2,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("D", cache),
      "AwesomeField");

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction21c);

    val asmInsn = (Instruction21c) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(Opcode.SGET_WIDE, asmInsn.opcode);

    val asmInsnRef = (FieldIdItem) asmInsn.getReferencedItem();
    assertEquals("Lcom/test/SomeClass;", asmInsnRef.getContainingClass().getTypeDescriptor());
    assertEquals("D", asmInsnRef.getFieldType().getTypeDescriptor());
    assertEquals("AwesomeField", asmInsnRef.getFieldName().getStringValue());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_StaticGetWide_WrongAllocation_Register() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(9);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_StaticGetWide(
      null,
      regTo1,
      regTo2,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("J", cache),
      "AwesomeField");

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_StaticGetWide_WrongAllocation_FollowUp() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo - 1);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_StaticGetWide(
      null,
      regTo1,
      regTo2,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("J", cache),
      "AwesomeField");

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }
}
