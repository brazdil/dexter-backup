package uk.ac.cam.db538.dexter.dex.method;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.Utils;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public abstract class DexMethod {

  @Getter @Setter private DexClass ParentClass;
  @Getter private final String Name;
  @Getter private final Set<AccessFlags> AccessFlagSet;
  @Getter private final DexType ReturnType;
  @Getter private final List<DexRegisterType> ArgumentTypes;

  public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags,
                   DexType returnType, List<DexRegisterType> argumentTypes) {
    ParentClass = parent;
    Name = name;
    AccessFlagSet = Utils.getNonNullAccessFlagSet(accessFlags);
    ReturnType = returnType;
    ArgumentTypes = (argumentTypes == null) ? new LinkedList<DexRegisterType>() : argumentTypes;
  }

  public static DexType parseReturnType(TypeIdItem item, DexParsingCache cache) {
	  return DexType.parse(item.getTypeDescriptor(), cache);
  }
  
  public static List<DexRegisterType> parseArgumentTypes(TypeListItem params, DexParsingCache cache) {
    val list = new LinkedList<DexRegisterType>();
    if (params != null) {
      for (val type : params.getTypes())
        list.add(DexRegisterType.parse(type.getTypeDescriptor(), cache));
    }
    return list;
  }

  public DexMethod(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    this(parent,
         methodInfo.method.getMethodName().getStringValue(),
         Utils.getAccessFlagSet(AccessFlags.getAccessFlagsForMethod(methodInfo.accessFlags)),
         parseReturnType(methodInfo.method.getPrototype().getReturnType(), parent.getParentFile().getParsingCache()),
         parseArgumentTypes(methodInfo.method.getPrototype().getParameters(), parent.getParentFile().getParsingCache()));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }

  public abstract boolean isVirtual();

  public abstract void instrument();

  protected abstract CodeItem generateCodeItem(DexFile outFile);

  public EncodedMethod writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val classType = cache.getType(ParentClass.getType());
    val methodName = cache.getStringConstant(Name);
    val methodPrototype = cache.getPrototype(ReturnType, ArgumentTypes);

    val methodItem = MethodIdItem.internMethodIdItem(outFile, classType, methodPrototype, methodName);
    return new EncodedMethod(methodItem, Utils.assembleAccessFlags(AccessFlagSet), generateCodeItem(outFile));
  }
}
