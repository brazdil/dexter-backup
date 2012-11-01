package uk.ac.cam.db538.dexter.dex;

import java.util.List;
import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexPurelyVirtualMethod extends DexMethod {

  public DexPurelyVirtualMethod(DexClass parent, String name, Set<AccessFlags> accessFlags,
                                DexType returnType, List<DexRegisterType> parameterTypes) {
    super(parent, name, accessFlags, returnType, parameterTypes);
  }

  public DexPurelyVirtualMethod(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, DexInstructionParsingException {
    super(parent, methodInfo);
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @Override
  public void instrument() {
  }
}
