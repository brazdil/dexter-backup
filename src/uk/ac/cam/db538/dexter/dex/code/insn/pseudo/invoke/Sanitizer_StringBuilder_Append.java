package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Sanitizer_StringBuilder_Append extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getClassHierarchy();
    val parsingCache = insn.getParentFile().getParsingCache();

    val insnInvoke = insn.getInstructionInvoke();

    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!insnInvoke.getMethodName().equals("append"))
      return false;

    if (!classHierarchy.isAncestor(insnInvoke.getClassType(),
                                   DexClassType.parse("Ljava/lang/StringBuilder;", parsingCache)))
      return false;

    return true;
  }

  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    // don't propagate the combined taint back into the first parameter (appended value)
    return this.generateExternalCallCode(insn, state, Collections.<Integer> emptySet(), Arrays.asList(1), false);
  }

}
