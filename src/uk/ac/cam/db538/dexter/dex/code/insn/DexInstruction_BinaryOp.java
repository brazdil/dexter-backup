package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_BinaryOp extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  public static enum Opcode {
    AddInt("add-int"),
    SubInt("sub-int"),
    MulInt("mul-int"),
    DivInt("div-int"),
    RemInt("rem-int"),
    AndInt("and-int"),
    OrInt("or-int"),
    XorInt("xor-int"),
    ShlInt("shl-int"),
    ShrInt("shr-int"),
    UshrInt("ushr-int"),
    AddFloat("add-float"),
    SubFloat("sub-float"),
    MulFloat("mul-float"),
    DivFloat("div-float"),
    RemFloat("rem-float");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case ADD_INT:
      case ADD_INT_2ADDR:
        return AddInt;
      case SUB_INT:
      case SUB_INT_2ADDR:
        return SubInt;
      case MUL_INT:
      case MUL_INT_2ADDR:
        return MulInt;
      case DIV_INT:
      case DIV_INT_2ADDR:
        return DivInt;
      case REM_INT:
      case REM_INT_2ADDR:
        return RemInt;
      case AND_INT:
      case AND_INT_2ADDR:
        return AndInt;
      case OR_INT:
      case OR_INT_2ADDR:
        return OrInt;
      case XOR_INT:
      case XOR_INT_2ADDR:
        return XorInt;
      case SHL_INT:
      case SHL_INT_2ADDR:
        return ShlInt;
      case SHR_INT:
      case SHR_INT_2ADDR:
        return ShrInt;
      case USHR_INT:
      case USHR_INT_2ADDR:
        return UshrInt;
      case ADD_FLOAT:
      case ADD_FLOAT_2ADDR:
        return AddFloat;
      case SUB_FLOAT:
      case SUB_FLOAT_2ADDR:
        return SubFloat;
      case MUL_FLOAT:
      case MUL_FLOAT_2ADDR:
        return MulFloat;
      case DIV_FLOAT:
      case DIV_FLOAT_2ADDR:
        return DivFloat;
      case REM_FLOAT:
      case REM_FLOAT_2ADDR:
        return RemFloat;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case AddInt:
        return org.jf.dexlib.Code.Opcode.ADD_INT;
      case SubInt:
        return org.jf.dexlib.Code.Opcode.SUB_INT;
      case MulInt:
        return org.jf.dexlib.Code.Opcode.MUL_INT;
      case DivInt:
        return org.jf.dexlib.Code.Opcode.DIV_INT;
      case RemInt:
        return org.jf.dexlib.Code.Opcode.REM_INT;
      case AndInt:
        return org.jf.dexlib.Code.Opcode.AND_INT;
      case OrInt:
        return org.jf.dexlib.Code.Opcode.OR_INT;
      case XorInt:
        return org.jf.dexlib.Code.Opcode.XOR_INT;
      case ShlInt:
        return org.jf.dexlib.Code.Opcode.SHL_INT;
      case ShrInt:
        return org.jf.dexlib.Code.Opcode.SHR_INT;
      case UshrInt:
        return org.jf.dexlib.Code.Opcode.USHR_INT;
      case AddFloat:
        return org.jf.dexlib.Code.Opcode.ADD_FLOAT;
      case SubFloat:
        return org.jf.dexlib.Code.Opcode.SUB_FLOAT;
      case MulFloat:
        return org.jf.dexlib.Code.Opcode.MUL_FLOAT;
      case DivFloat:
        return org.jf.dexlib.Code.Opcode.DIV_FLOAT;
      case RemFloat:
        return org.jf.dexlib.Code.Opcode.REM_FLOAT;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert2addr(Opcode opcode) {
      switch (opcode) {
      case AddInt:
        return org.jf.dexlib.Code.Opcode.ADD_INT_2ADDR;
      case SubInt:
        return org.jf.dexlib.Code.Opcode.SUB_INT_2ADDR;
      case MulInt:
        return org.jf.dexlib.Code.Opcode.MUL_INT_2ADDR;
      case DivInt:
        return org.jf.dexlib.Code.Opcode.DIV_INT_2ADDR;
      case RemInt:
        return org.jf.dexlib.Code.Opcode.REM_INT_2ADDR;
      case AndInt:
        return org.jf.dexlib.Code.Opcode.AND_INT_2ADDR;
      case OrInt:
        return org.jf.dexlib.Code.Opcode.OR_INT_2ADDR;
      case XorInt:
        return org.jf.dexlib.Code.Opcode.XOR_INT_2ADDR;
      case ShlInt:
        return org.jf.dexlib.Code.Opcode.SHL_INT_2ADDR;
      case ShrInt:
        return org.jf.dexlib.Code.Opcode.SHR_INT_2ADDR;
      case UshrInt:
        return org.jf.dexlib.Code.Opcode.USHR_INT_2ADDR;
      case AddFloat:
        return org.jf.dexlib.Code.Opcode.ADD_FLOAT_2ADDR;
      case SubFloat:
        return org.jf.dexlib.Code.Opcode.SUB_FLOAT_2ADDR;
      case MulFloat:
        return org.jf.dexlib.Code.Opcode.MUL_FLOAT_2ADDR;
      case DivFloat:
        return org.jf.dexlib.Code.Opcode.DIV_FLOAT_2ADDR;
      case RemFloat:
        return org.jf.dexlib.Code.Opcode.REM_FLOAT_2ADDR;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTarget;
  @Getter private final DexRegister RegSourceA;
  @Getter private final DexRegister RegSourceB;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_BinaryOp(DexRegister target, DexRegister sourceA, DexRegister sourceB, Opcode opcode) {
    RegTarget = target;
    RegSourceA = sourceA;
    RegSourceB = sourceB;
    InsnOpcode = opcode;
  }

  public DexInstruction_BinaryOp(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction23x && Opcode.convert(insn.opcode) != null) {

      val insnBinaryOp = (Instruction23x) insn;
      RegTarget = parsingState.getRegister(insnBinaryOp.getRegisterA());
      RegSourceA = parsingState.getRegister(insnBinaryOp.getRegisterB());
      RegSourceB = parsingState.getRegister(insnBinaryOp.getRegisterC());
      InsnOpcode = Opcode.convert(insn.opcode);

    } else if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnBinaryOp2addr = (Instruction12x) insn;
      RegTarget = RegSourceA = parsingState.getRegister(insnBinaryOp2addr.getRegisterA());
      RegSourceB = parsingState.getRegister(insnBinaryOp2addr.getRegisterB());
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget.getId() +
           ", v" + RegSourceA.getId() + ", v" + RegSourceB.getId();
  }

  @Override
  public DexCodeElement[] instrument(TaintRegisterMap mapping) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_BinaryOp(
               mapping.getTaintRegister(RegTarget),
               mapping.getTaintRegister(RegSourceA),
               mapping.getTaintRegister(RegSourceB),
               DexInstruction_BinaryOp.Opcode.OrInt)
           };
  }

  @Override
  protected DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { RegTarget, RegSourceA, RegSourceB };
  }
}
