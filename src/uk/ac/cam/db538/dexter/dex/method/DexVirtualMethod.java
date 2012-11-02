package uk.ac.cam.db538.dexter.dex.method;

import java.util.List;
import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexVirtualMethod extends DexMethodWithCode {

  public DexVirtualMethod(DexClass parent, EncodedMethod methodInfo)
  throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
  }

  public DexVirtualMethod(DexClass parent, String name,
                          Set<AccessFlags> accessFlags, DexType returnType,
                          List<DexRegisterType> parameterTypes, DexCode code, boolean direct) {
    super(parent, name, accessFlags, returnType, parameterTypes, code, direct);
  }
}
