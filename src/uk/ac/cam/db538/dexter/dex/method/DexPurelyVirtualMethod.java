package uk.ac.cam.db538.dexter.dex.method;

import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexPurelyVirtualMethod extends DexMethod {

  public DexPurelyVirtualMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype) {
    super(parent, name, accessFlags, prototype);
  }

  public DexPurelyVirtualMethod(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @Override
  public void instrument() { }

  @Override
  protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
    return null;
  }
}
