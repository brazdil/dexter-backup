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

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.Utils;

public class DexInstruction_FilledNewArray extends DexInstruction {

  @Getter private final List<DexSingleRegister> argumentRegisters;
  @Getter private final DexArrayType arrayType;

  public DexInstruction_FilledNewArray(List<DexSingleRegister> arrayElems, DexArrayType arrayType, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.arrayType = arrayType;
    if (this.arrayType.getElementType().isWide())
      throw new InstructionParseError("FilledNewArray doesn't support wide types");

    this.argumentRegisters = Utils.finalList(arrayElems);
  }

  public static DexInstruction_FilledNewArray parse(Instruction insn, CodeParserState parsingState) {
    val argumentRegisters = new LinkedList<DexSingleRegister>();

    if (insn instanceof Instruction35c && insn.opcode == Opcode.FILLED_NEW_ARRAY) {

      val insnFilledNewArray = (Instruction35c) insn;

      switch (insnFilledNewArray.getRegCount()) {
      case 5:
        argumentRegisters.add(0, parsingState.getSingleRegister(insnFilledNewArray.getRegisterA()));
      case 4:
        argumentRegisters.add(0, parsingState.getSingleRegister(insnFilledNewArray.getRegisterG()));
      case 3:
        argumentRegisters.add(0, parsingState.getSingleRegister(insnFilledNewArray.getRegisterF()));
      case 2:
        argumentRegisters.add(0, parsingState.getSingleRegister(insnFilledNewArray.getRegisterE()));
      case 1:
        argumentRegisters.add(0, parsingState.getSingleRegister(insnFilledNewArray.getRegisterD()));
      case 0:
        break;
      default:
        throw new InstructionParseError("Unexpected number of FilledNewArray argument registers");
      }

    } else if (insn instanceof Instruction3rc && insn.opcode == Opcode.FILLED_NEW_ARRAY_RANGE) {

      val insnFilledNewArray = (Instruction3rc) insn;

      val startRegister = insnFilledNewArray.getStartRegister();
      for (int i = 0; i < insnFilledNewArray.getRegCount(); ++i)
        argumentRegisters.add(parsingState.getSingleRegister(startRegister + i));

    } else
      throw FORMAT_EXCEPTION;

    val hierarchy = parsingState.getHierarchy();
    val arrayType = DexArrayType.parse(((TypeIdItem)((InstructionWithReference) insn).getReferencedItem()).getTypeDescriptor(), hierarchy.getTypeCache());

    return new DexInstruction_FilledNewArray(argumentRegisters, arrayType, hierarchy);
  }

  @Override
  public String toString() {
    val str = new StringBuffer("filled-new-array (");
    boolean first = true;
    for (val arg : argumentRegisters) {
      if (first)
        first = false;
      else
        str.append(", ");
      str.append(arg.toString());
    }
    str.append("), ");
    str.append(arrayType.getDescriptor());

    return str.toString();
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return new HashSet<DexRegister>(argumentRegisters);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error;
  }
  
}
