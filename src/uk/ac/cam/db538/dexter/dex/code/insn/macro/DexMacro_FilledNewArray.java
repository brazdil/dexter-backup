package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.type.DexType_Primitive;

public class DexMacro_FilledNewArray extends DexMacro {

  @Getter private final DexInstruction_FilledNewArray instructionFilledNewArray;
  @Getter private final DexInstruction_MoveResult instructionMoveResult;

  public DexMacro_FilledNewArray(DexCode methodCode, DexInstruction_FilledNewArray instructionFilledNewArray, DexInstruction_MoveResult insnMoveResult) {
    super(methodCode);

    this.instructionFilledNewArray = instructionFilledNewArray;
    this.instructionMoveResult = insnMoveResult;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    return createList(
             (DexCodeElement) instructionFilledNewArray,
             (DexCodeElement) instructionMoveResult);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    if (instructionFilledNewArray.getArgumentRegisters().isEmpty())
      return;

    val replacement = new LinkedList<DexCodeElement>();
    boolean first = true;

    val regCombinedTaint = new DexRegister();
    if (instructionFilledNewArray.getArrayType().getElementType() instanceof DexType_Primitive) {
      for (val regArg : instructionFilledNewArray.getArgumentRegisters()) {
        if (first) {
          first = false;
          replacement.add(new DexInstruction_Move(code, regCombinedTaint, state.getTaintRegister(regArg), false));
        } else {
          replacement.add(new DexInstruction_BinaryOp(code, regCombinedTaint, regCombinedTaint, state.getTaintRegister(regArg), Opcode_BinaryOp.OrInt));
        }
      }
    } else {
      val regObjectTaint = new DexRegister();
      for (val regArg : instructionFilledNewArray.getArgumentRegisters()) {
        if (first) {
          first = false;
          replacement.add(new DexMacro_GetObjectTaint(code, regCombinedTaint, regArg));
        } else {
          replacement.add(new DexMacro_GetObjectTaint(code, regObjectTaint, regArg));
          replacement.add(new DexInstruction_BinaryOp(code, regCombinedTaint, regCombinedTaint, regObjectTaint, Opcode_BinaryOp.OrInt));
        }
      }
    }

    replacement.add(this);
    replacement.add(new DexMacro_SetObjectTaint(code, instructionMoveResult.getRegTo(), regCombinedTaint));

    code.replace(this, replacement);
  }

@Override
public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
}

}
