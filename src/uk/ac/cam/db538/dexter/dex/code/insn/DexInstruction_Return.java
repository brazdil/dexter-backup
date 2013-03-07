package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

public class DexInstruction_Return extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final boolean objectMoving;

  public DexInstruction_Return(DexCode methodCode, DexRegister from, boolean objectMoving) {
    super(methodCode);

    this.regFrom = from;
    this.objectMoving = objectMoving;
  }

  public DexInstruction_Return(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.RETURN || insn.opcode == Opcode.RETURN_OBJECT)) {

      val insnReturn = (Instruction11x) insn;
      regFrom = parsingState.getRegister(insnReturn.getRegisterA());
      objectMoving = insn.opcode == Opcode.RETURN_OBJECT;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "return" + (objectMoving ? "-object" : "") +
           " " + regFrom.getOriginalIndexString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rFrom = state.getRegisterAllocation().get(regFrom);
    val opcode = objectMoving ? Opcode.RETURN_OBJECT : Opcode.RETURN;

    if (fitsIntoBits_Unsigned(rFrom, 8))
      return new Instruction[] {
               new Instruction11x(opcode, (short) rFrom)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public boolean cfgExitsMethod() {
    return true;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  protected gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regFrom))
      return (objectMoving) ? gcRegType.Object : gcRegType.PrimitiveSingle;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regFrom, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newFrom = (toRefs) ? mapping.get(regFrom) : regFrom;
    return new DexInstruction_Return(getMethodCode(), newFrom, objectMoving);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val printDebug = state.getCache().isInsertDebugLogging();

    val code = getMethodCode();
    val insnPrintDebug = new DexPseudoinstruction_PrintStringConst(
      code,
      "$# exiting method " +
      getParentClass().getType().getPrettyName() +
      "->" + getParentMethod().getName(),
      true);

    if (!objectMoving) {
      val dex = getParentFile();
      val parentMethod = getParentMethod();

      val regResSemaphore = new DexRegister();

      val insnGetSRES = new DexInstruction_StaticGet(code, regResSemaphore, dex.getMethodCallHelper_SRes());
      val insnAcquireSRES = new DexInstruction_Invoke(
        code,
        (DexClassType) dex.getMethodCallHelper_SRes().getType(),
        "acquire",
        new DexPrototype(DexVoid.parse("V", null), null),
        Arrays.asList(regResSemaphore),
        Opcode_Invoke.Virtual);
      val insnSetRES = new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom), dex.getMethodCallHelper_Res());

      if (parentMethod.isVirtual()) {
        val labelSkipTaintPassing = new DexLabel(code);

        val insnTestInternalClassAnnotation = new DexInstruction_IfTestZero(
          code,
          state.getInternalClassAnnotationRegister(),
          labelSkipTaintPassing,
          Opcode_IfTestZero.eqz);

        if (printDebug) {
          code.replace(this, new DexCodeElement[] {
                         insnTestInternalClassAnnotation,
                         insnGetSRES,
                         insnAcquireSRES,
                         insnSetRES,
                         labelSkipTaintPassing,
                         insnPrintDebug,
                         this
                       });
        } else {
          code.replace(this, new DexCodeElement[] {
                         insnTestInternalClassAnnotation,
                         insnGetSRES,
                         insnAcquireSRES,
                         insnSetRES,
                         labelSkipTaintPassing,
                         this
                       });
        }
      } else {
        if (printDebug) {
          code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, insnPrintDebug, this});
        } else {
          code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, this});
        }
      }
    } else {
      if (printDebug) {
        code.replace(this, new DexCodeElement[] {insnPrintDebug, this});
      }
    }
  }
}
