package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_FilledNewArray extends DexInstruction {

  @Getter private final List<DexRegister> argumentRegisters;
  @Getter private final DexArrayType arrayType;

  public DexInstruction_FilledNewArray(DexCode methodCode, List<DexRegister> arrayElems, DexArrayType arrayType) {
    super(methodCode);

    this.argumentRegisters = new LinkedList<DexRegister>(arrayElems);
    this.arrayType = arrayType;
    if (this.arrayType.getElementType().isWide())
      throw new InstructionParsingException("FilledNewArray doesn't support wide types");

  }

  public DexInstruction_FilledNewArray(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    this.argumentRegisters = new LinkedList<DexRegister>();

    if (insn instanceof Instruction35c && insn.opcode == Opcode.FILLED_NEW_ARRAY) {

      val insnFilledNewArray = (Instruction35c) insn;

      switch (insnFilledNewArray.getRegCount()) {
      case 5:
        argumentRegisters.add(0, parsingState.getRegister(insnFilledNewArray.getRegisterA()));
      case 4:
        argumentRegisters.add(0, parsingState.getRegister(insnFilledNewArray.getRegisterG()));
      case 3:
        argumentRegisters.add(0, parsingState.getRegister(insnFilledNewArray.getRegisterF()));
      case 2:
        argumentRegisters.add(0, parsingState.getRegister(insnFilledNewArray.getRegisterE()));
      case 1:
        argumentRegisters.add(0, parsingState.getRegister(insnFilledNewArray.getRegisterD()));
      case 0:
        break;
      default:
        throw new InstructionParsingException("Unexpected number of FilledNewArray argument registers");
      }

    } else if (insn instanceof Instruction3rc && insn.opcode == Opcode.FILLED_NEW_ARRAY_RANGE) {

      val insnFilledNewArray = (Instruction3rc) insn;

      val startRegister = insnFilledNewArray.getStartRegister();
      for (int i = 0; i < insnFilledNewArray.getRegCount(); ++i)
        argumentRegisters.add(parsingState.getRegister(startRegister + i));

    } else
      throw FORMAT_EXCEPTION;

    this.arrayType = DexArrayType.parse(((TypeIdItem)((InstructionWithReference) insn).getReferencedItem()).getTypeDescriptor(), parsingState.getCache());
    if (this.arrayType.getElementType().isWide())
      throw new InstructionParsingException("FilledNewArray doesn't support wide types");

  }

  private boolean assemblesToRange() {
    return argumentRegisters.size() > 5;
  }

  @Override
  public String getOriginalAssembly() {
    val str = new StringBuffer("filled-new-array (");
    boolean first = true;
    for (val arg : argumentRegisters) {
      if (first)
        first = false;
      else
        str.append(", ");
      str.append(arg.getOriginalIndexString());
    }
    str.append("), ");
    str.append(arrayType.getDescriptor());

    return str.toString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int[] r = new int[argumentRegisters.size()];
    for (int i = 0; i < r.length; ++i)
      r[i] = regAlloc.get(argumentRegisters.get(i));

    val typeItem = state.getCache().getType(arrayType);

    if (assemblesToRange()) {
      if (!fitsIntoBits_Unsigned(r.length, 8))
        return throwCannotAssembleException("Too many argument registers");

      val firstReg = r[0];
      for (int i = 1; i < r.length; ++i)
        if (!(r[i] == r[i - 1] + 1))
          return throwCannotAssembleException("Argument registers don't form an interval");

      return new Instruction[] {
               new Instruction3rc(Opcode.FILLED_NEW_ARRAY_RANGE,
                                  (short) r.length,
                                  firstReg,
                                  typeItem)
             };
    } else {
      for (int regNum : r)
        if (!fitsIntoBits_Unsigned(regNum, 4))
          return throwCannotAssembleException("Register numbers don't fit into 4 bits");

      return new Instruction[] {
               new Instruction35c(Opcode.FILLED_NEW_ARRAY,
                                  r.length,
                                  (byte) ((r.length >= 1) ? r[0] : 0),
                                  (byte) ((r.length >= 2) ? r[1] : 0),
                                  (byte) ((r.length >= 3) ? r[2] : 0),
                                  (byte) ((r.length >= 4) ? r[3] : 0),
                                  (byte) ((r.length >= 5) ? r[4] : 0),
                                  typeItem)
             };
    }

  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return new HashSet<DexRegister>(argumentRegisters);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error;
  }
  
}
