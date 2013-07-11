package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction31t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexInstruction_FillArray extends DexInstruction {

  @Getter private final DexRegister regArray;
  @Getter private final DexLabel arrayTable;

  public DexInstruction_FillArray(DexCode methodCode, DexRegister array, DexLabel arrayTable) {
    super(methodCode);

    this.regArray = array;
    this.arrayTable = arrayTable;

    setUp();
  }

  public DexInstruction_FillArray(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction31t && insn.opcode == Opcode.FILL_ARRAY_DATA) {

      val insnFillArrayData = (Instruction31t) insn;
      int dataTableOffset = insnFillArrayData.getTargetAddressOffset();

      this.regArray = parsingState.getRegister(insnFillArrayData.getRegisterA());
      this.arrayTable = parsingState.getLabel(dataTableOffset);

      parsingState.registerParentInstruction(this, dataTableOffset);
      setUp();

    } else
      throw FORMAT_EXCEPTION;
  }

  private void setUp() {
    this.arrayTable.setEvenAligned(true);
  }

  @Override
  public String getOriginalAssembly() {
    return "fill-array " + regArray.getOriginalIndexString() + ", L" + arrayTable.getOriginalAbsoluteOffset();
  }

  public Pair<DexCodeElement, DexCodeElement> gcReplaceFillArrayDataReference(DexInstruction_FillArray replacementFillArray) {
    val code = getMethodCode();
    val insnTarget = code.getFollowingInstruction(arrayTable);

    if (insnTarget instanceof DexInstruction_FillArrayData) {
      val insnFillArrayData = (DexInstruction_FillArrayData) insnTarget;
      return new Pair<DexCodeElement, DexCodeElement>(insnFillArrayData,
             new DexInstruction_FillArrayData(code,
                                              replacementFillArray,
                                              insnFillArrayData.getElementData()));
    } else
      throw new RuntimeException("Target instruction of FillArray is not FillArrayData");
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
    return createSet((DexCodeElement) arrayTable);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
