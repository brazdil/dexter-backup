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

public class DexInstruction_FillArray extends DexInstruction {

  @Getter private final DexRegister regArray;
  @Getter private final DexLabel arrayTable;

  public DexInstruction_FillArray(DexCode methodCode, DexRegister array, DexLabel arrayTable) {
    super(methodCode);

    this.regArray = array;
    this.arrayTable = arrayTable;
  }

  public DexInstruction_FillArray(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction31t && insn.opcode == Opcode.FILL_ARRAY_DATA) {

      val insnFillArrayData = (Instruction31t) insn;
      int dataTableOffset = insnFillArrayData.getTargetAddressOffset();

      this.regArray = parsingState.getRegister(insnFillArrayData.getRegisterA());
      this.arrayTable = parsingState.getLabel(dataTableOffset);

      parsingState.registerParentInstruction(this, dataTableOffset);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "fill-array v" + regArray.getOriginalIndexString() + ", L" + arrayTable.getOriginalAbsoluteOffset();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_FillArray(getMethodCode(), mapping.get(regArray), arrayTable);
  }
}
