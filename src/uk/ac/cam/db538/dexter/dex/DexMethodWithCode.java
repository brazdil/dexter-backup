package uk.ac.cam.db538.dexter.dex;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction.TaintRegisterMap;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexMethodWithCode extends DexMethod {

  @Getter private DexCode Code;
  @Getter private final boolean Direct;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexType returnType, List<DexRegisterType> parameterTypes,
                           DexCode code, boolean direct) {
    super(parent, name, accessFlags, returnType, parameterTypes);
    Code = code;
    Direct = direct;
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
    Code = DexInstruction.parseMethodCode(methodInfo.codeItem.getInstructions(), parent.getParentFile().getParsingCache());
    Direct = methodInfo.isDirect();
  }

  @Override
  public boolean isVirtual() {
    return !Direct;
  }

  @Override
  public void instrument() {
    TaintRegisterMap taintRegs = new TaintRegisterMap(Code);
    val newCode = new DexCode();
    for (val elem : Code) {
      if (elem instanceof DexInstruction) {
        val insn = (DexInstruction) elem;
        newCode.addAll(Arrays.asList(insn.instrument(taintRegs)));
      } else
        newCode.add(elem);
    }
    Code = newCode;
  }
}
