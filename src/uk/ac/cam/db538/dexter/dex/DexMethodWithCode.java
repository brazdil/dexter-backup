package uk.ac.cam.db538.dexter.dex;

import java.util.List;
import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;

public class DexMethodWithCode extends DexMethod {

  @Getter private final List<DexCodeElement> Code;
  private final boolean Direct;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexType returnType, List<DexRegisterType> parameterTypes,
                           List<DexCodeElement> code, boolean direct) {
    super(parent, name, accessFlags, returnType, parameterTypes);
    Code = code;
    Direct = direct;
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, DexInstructionParsingException {
    super(parent, methodInfo);
    Code = DexInstruction.parse(methodInfo.codeItem.getInstructions(), parent.getParentFile().getParsingCache());
    Direct = methodInfo.isDirect();
  }

  @Override
  public boolean isVirtual() {
    return !Direct;
  }
}