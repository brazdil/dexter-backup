package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_UnaryOp extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_UnaryOp insnOpcode;

  public DexInstruction_UnaryOp(DexCode methodCode, DexRegister to, DexRegister from, Opcode_UnaryOp opcode) {
    super(methodCode);
    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_UnaryOp(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOp.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnUnaryOp.getRegisterA());
      regFrom = parsingState.getRegister(insnUnaryOp.getRegisterB());
      insnOpcode = Opcode_UnaryOp.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + regFrom.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    code.replace(this, new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom), false)
                 });
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rFrom = regAlloc.get(regFrom);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rFrom, 4))
      return new Instruction[] { new Instruction12x(Opcode_UnaryOp.convert(insnOpcode), (byte) rTo, (byte) rFrom) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
