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

  public DexInstruction_Switch(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
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
    return (packed ? "packed" : "sparse") + "-switch " + regTest.getOriginalIndexString() + ", L" + switchTable.getOriginalAbsoluteOffset();
  }

  public Pair<DexCodeElement, DexCodeElement> gcReplaceSwitchTableParentReference(DexInstruction_Switch replacementSwitch) {
    val code = getMethodCode();
    val insnSwitchData = code.getFollowingInstruction(switchTable);

    if (insnSwitchData instanceof DexInstruction_PackedSwitchData) {
      val insnPackedSwitchData = (DexInstruction_PackedSwitchData) insnSwitchData;
      return new Pair<DexCodeElement, DexCodeElement>(insnPackedSwitchData,
             new DexInstruction_PackedSwitchData(code,
                 replacementSwitch,
                 insnPackedSwitchData.getFirstKey(),
                 insnPackedSwitchData.getTargets()));
    } else if (insnSwitchData instanceof DexInstruction_SparseSwitchData) {
      val insnSparseSwitchData = (DexInstruction_SparseSwitchData) insnSwitchData;
      return new Pair<DexCodeElement, DexCodeElement>(insnSparseSwitchData,
             new DexInstruction_SparseSwitchData(code,
                 replacementSwitch,
                 insnSparseSwitchData.getKeyTargetPairs()));
    } else
      throw new RuntimeException("Target instruction of Switch is not Packed/SparseSwitchData");
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
    return createSet(getNextCodeElement(), (DexCodeElement) switchTable);
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regTest);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
