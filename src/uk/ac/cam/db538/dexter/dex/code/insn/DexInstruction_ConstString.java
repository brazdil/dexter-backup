package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction31c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_ConstString extends DexInstruction {

  @Getter private final DexSingleRegister regTo;
  @Getter private final String stringConstant;

  public DexInstruction_ConstString(DexSingleRegister to, String value, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	
    regTo = to;
    stringConstant = value;
  }

  public static DexInstruction_ConstString parse(Instruction insn, CodeParserState parsingState) {
    DexSingleRegister regTo;
    String stringConstant;
	  
    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_STRING) {

      val insnConstString = (Instruction21c) insn;
      regTo = parsingState.getSingleRegister(insnConstString.getRegisterA());
      stringConstant = ((StringIdItem) insnConstString.getReferencedItem()).getStringValue();

    } else if (insn instanceof Instruction31c && insn.opcode == Opcode.CONST_STRING_JUMBO) {

      val insnConstStringJumbo = (Instruction31c) insn;
      regTo = parsingState.getSingleRegister(insnConstStringJumbo.getRegisterA());
      stringConstant = ((StringIdItem) insnConstStringJumbo.getReferencedItem()).getStringValue();

    } else
      throw FORMAT_EXCEPTION;
    
    return new DexInstruction_ConstString(regTo, stringConstant, parsingState.getHierarchy());
  }

  @Override
  public String toString() {
    String escapedVal = stringConstant;
    if (escapedVal.length() > 15)
      escapedVal = escapedVal.substring(0, 15) + "...";
    return "const-string " + regTo.toString() + ", \"" + escapedVal + "\"";
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error;
  }
  
}
