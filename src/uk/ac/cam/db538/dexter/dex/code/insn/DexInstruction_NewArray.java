package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_NewArray extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regSize;
  @Getter private final DexArrayType value;

  public DexInstruction_NewArray(DexCode methodCode, DexRegister to, DexRegister size, DexArrayType value) {
    super(methodCode);

    this.regTo = to;
    this.regSize = size;
    this.value = value;
  }

  public DexInstruction_NewArray(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && insn.opcode == Opcode.NEW_ARRAY) {

      val insnNewArray = (Instruction22c) insn;
      regTo = parsingState.getRegister(insnNewArray.getRegisterA());
      regSize = parsingState.getRegister(insnNewArray.getRegisterB());
      value = DexArrayType.parse(
                ((TypeIdItem) insnNewArray.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "new-array v" + regTo.getOriginalIndexString() + ", v" + regSize.getOriginalIndexString() +
           ", " + value.getDescriptor();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rSize = regAlloc.get(regSize);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rSize, 4)) {
      return new Instruction[] {
               new Instruction22c(Opcode.NEW_ARRAY, (byte) rTo, (byte) rSize, state.getCache().getType(value))
             };
    } else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regSize);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT),
             new GcRangeConstraint(regSize, ColorRange.RANGE_4BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_NewArray(getMethodCode(), mapping.get(regTo), mapping.get(regSize), value);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }
}
