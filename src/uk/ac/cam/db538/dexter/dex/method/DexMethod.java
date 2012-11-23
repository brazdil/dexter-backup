package uk.ac.cam.db538.dexter.dex.method;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public abstract class DexMethod {

  @Getter @Setter private DexClass ParentClass;
  @Getter private final String Name;
  @Getter private final Set<AccessFlags> AccessFlagSet;
  @Getter private final DexPrototype Prototype;

  public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype) {
    ParentClass = parent;
    Name = name;
    AccessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    Prototype = prototype;
  }


  public DexMethod(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    this(parent,
         methodInfo.method.getMethodName().getStringValue(),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForMethod(methodInfo.accessFlags)),
         new DexPrototype(methodInfo.method.getPrototype(), parent.getParentFile().getParsingCache()));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }

  public abstract boolean isVirtual();

  public abstract void instrument();

  protected abstract CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache);

  public EncodedMethod writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val classType = cache.getType(ParentClass.getType());
    val methodName = cache.getStringConstant(Name);
    val methodPrototype = cache.getPrototype(Prototype);

    val methodItem = MethodIdItem.internMethodIdItem(outFile, classType, methodPrototype, methodName);
    return new EncodedMethod(methodItem, DexUtils.assembleAccessFlags(AccessFlagSet), generateCodeItem(outFile, cache));
  }

  public static Cache<Triple<DexClassType, DexPrototype, String>, MethodIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<Triple<DexClassType, DexPrototype, String>, MethodIdItem>() {
      @Override
      protected MethodIdItem createNewEntry(Triple<DexClassType, DexPrototype, String> key) {
        return MethodIdItem.internMethodIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getPrototype(key.getValB()),
                 cache.getStringConstant(key.getValC()));
      }
    };
  }

}
