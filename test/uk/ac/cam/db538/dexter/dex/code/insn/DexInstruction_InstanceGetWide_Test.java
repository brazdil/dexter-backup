package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexInstruction_InstanceGetWide_Test {

  @Test
  public void testParse_InstanceGetWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction22c(Opcode.IGET_WIDE, (byte) 0, (byte) 1, Utils.getFieldItem("Lcom/example/MyClass5;", "J", "TestField5")),
        new Instruction22c(Opcode.IGET_WIDE, (byte) 2, (byte) 3, Utils.getFieldItem("Lcom/example/MyClass6;", "D", "TestField6"))
      }, new String[] {
        "iget-wide v0, {v1}com.example.MyClass5.TestField5",
        "iget-wide v2, {v3}com.example.MyClass6.TestField6",
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGetWide_WrongType() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_WIDE, (byte) 0, (byte) 1, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test
  public void testAssemble_InstanceGetWide() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(4);
    val regNObject = Utils.numFitsInto_Unsigned(4) - 1;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regObject = new DexRegister(regNObject);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regObject);

    val insn = new DexInstruction_InstanceGetWide(
      null,
      regTo1,
      regTo2,
      regObject,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("D", cache),
      "AwesomeField");

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22c);

    val asmInsn = (Instruction22c) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(regNObject, asmInsn.getRegisterB());
    assertEquals(Opcode.IGET_WIDE, asmInsn.opcode);

    val asmInsnRef = (FieldIdItem) asmInsn.getReferencedItem();
    assertEquals("Lcom/test/SomeClass;", asmInsnRef.getContainingClass().getTypeDescriptor());
    assertEquals("D", asmInsnRef.getFieldType().getTypeDescriptor());
    assertEquals("AwesomeField", asmInsnRef.getFieldName().getStringValue());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_InstanceGetWide_WrongAllocation_RegisterTo() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(5);
    val regNObject = Utils.numFitsInto_Unsigned(4);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regObject = new DexRegister(regNObject);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regObject);

    val insn = new DexInstruction_InstanceGetWide(
      null,
      regTo1,
      regTo2,
      regObject,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("D", cache),
      "AwesomeField");

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_InstanceGetWide_WrongAllocation_RegisterObject() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(4);
    val regNObject = Utils.numFitsInto_Unsigned(5);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regObject = new DexRegister(regNObject);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regObject);

    val insn = new DexInstruction_InstanceGetWide(
      null,
      regTo1,
      regTo2,
      regObject,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("D", cache),
      "AwesomeField");

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_InstanceGetWide_WrongAllocation_FollowUp() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(4);
    val regNObject = Utils.numFitsInto_Unsigned(4) - 1;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo - 2);
    val regObject = new DexRegister(regNObject);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regObject);

    val insn = new DexInstruction_InstanceGetWide(
      null,
      regTo1,
      regTo2,
      regObject,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("D", cache),
      "AwesomeField");

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }
}
