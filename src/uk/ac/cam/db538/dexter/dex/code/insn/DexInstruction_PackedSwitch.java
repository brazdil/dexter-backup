package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction31t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_PackedSwitch extends DexInstruction {

  @Getter private final DexRegister regTest;
  @Getter private final DexLabel switchTable;

  public DexInstruction_PackedSwitch(DexCode methodCode, DexRegister test, DexLabel switchTable) {
    super(methodCode);

    this.regTest = test;
    this.switchTable = switchTable;
  }

  public DexInstruction_PackedSwitch(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction31t) {

      val insnPackedSwitch = (Instruction31t) insn;
      int dataTableOffset = insnPackedSwitch.getTargetAddressOffset();

      this.regTest = parsingState.getRegister(insnPackedSwitch.getRegisterA());
      this.switchTable = parsingState.getLabel(dataTableOffset);

      parsingState.registerParentInstruction(this, dataTableOffset);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "packed-switch v" + regTest.getOriginalIndexString() + ", L" + switchTable.getOriginalAbsoluteOffset();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_PackedSwitch(getMethodCode(), regTest, switchTable);
  }
}
