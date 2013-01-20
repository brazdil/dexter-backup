package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.Arrays;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class ContentResolverInstrumentor extends FallbackInstrumentor {

  private boolean fitsAPI1(DexPrototype methodPrototype) {
    val methodParamTypes = methodPrototype.getParameterTypes();
    return methodParamTypes.size() == 5 &&
           methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
           methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
           methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
           methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
           methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;"); // sortOrder
  }

  private boolean fitsAPI16(DexPrototype methodPrototype) {
    val methodParamTypes = methodPrototype.getParameterTypes();
    return methodParamTypes.size() == 6 &&
           methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
           methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
           methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
           methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
           methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;") && // sortOrder
           methodParamTypes.get(5).getDescriptor().equals("Landroid/os/CancellationSignal;"); // cancellationSignal
  }

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getClassHierarchy();
    val parsingCache = insn.getParentFile().getParsingCache();

    val insnInvoke = insn.getInstructionInvoke();

    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!insnInvoke.getMethodName().equals("query"))
      return false;

    if (!classHierarchy.isAncestor(insnInvoke.getClassType(),
                                   DexClassType.parse("Landroid/content/ContentResolver;", parsingCache)))
      return false;

    if (!fitsAPI1(insnInvoke.getMethodPrototype()) && !fitsAPI16(insnInvoke.getMethodPrototype()))
      return false;

    return true;
  }

  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);

    val methodCode = insn.getMethodCode();
    val parsingCache = insn.getParentFile().getParsingCache();
    val typeString = DexClassType.parse("Ljava/lang/String;", parsingCache);
    val preCode = new NoDuplicatesList<DexCodeElement>(fallback.getValA().size() + 20);

    // add Log entry with the query string
    val regStrTag = new DexRegister();
    val regStrQuery = new DexRegister();
    preCode.add(new DexInstruction_ConstString(methodCode, regStrTag, "DexterQuery"));
    preCode.add(new DexInstruction_Invoke(methodCode,
                                          DexClassType.parse("Landroid/net/Uri;", parsingCache),
                                          "toString",
                                          new DexPrototype(typeString, null),
                                          Arrays.asList(new DexRegister[] { insn.getInstructionInvoke().getArgumentRegisters().get(1) }),
                                          Opcode_Invoke.Virtual));
    preCode.add(new DexInstruction_MoveResult(methodCode, regStrQuery, true));
    preCode.add(new DexInstruction_Invoke(methodCode,
                                          DexClassType.parse("Landroid/util/Log;", parsingCache),
                                          "w",
                                          new DexPrototype(DexType.parse("I", parsingCache),
                                              Arrays.asList(new DexRegisterType[] { typeString, typeString })),
                                          Arrays.asList(new DexRegister[] { regStrTag, regStrQuery }),
                                          Opcode_Invoke.Static));
    preCode.addAll(fallback.getValA());

    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(preCode, fallback.getValB());
  }

}
