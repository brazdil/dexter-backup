package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction31t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_Switch extends DexInstruction {

  @Getter private final DexRegister regTest;
  @Getter private final DexLabel switchTable;
  @Getter private final boolean packed;

  public DexInstruction_Switch(DexCode methodCode, DexRegister test, DexLabel switchTable, boolean isPacked) {
    super(methodCode);

    this.regTest = test;
    this.switchTable = switchTable;
    this.packed = isPacked;

    setUp();
  }

  public DexInstruction_Switch(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction31t &&
        (insn.opcode == Opcode.PACKED_SWITCH || insn.opcode == Opcode.SPARSE_SWITCH)) {

      val insnSwitch = (Instruction31t) insn;
      int dataTableOffset = insnSwitch.getTargetAddressOffset();

      this.regTest = parsingState.getRegister(insnSwitch.getRegisterA());
      this.switchTable = parsingState.getLabel(dataTableOffset);
      this.packed = (insn.opcode == Opcode.PACKED_SWITCH);

      parsingState.registerParentInstruction(this, dataTableOffset);
      setUp();

    } else
      throw FORMAT_EXCEPTION;
  }

  private void setUp() {
    this.switchTable.setEvenAligned(true);
  }

  @Override
  public String getOriginalAssembly() {
    return (packed ? "packed" : "sparse") + "-switch v" + regTest.getOriginalIndexString() + ", L" + switchTable.getOriginalAbsoluteOffset();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_Switch(getMethodCode(), mapping.get(regTest), switchTable, packed);
  }
}
