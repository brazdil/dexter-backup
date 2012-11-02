package uk.ac.cam.db538.dexter.dex;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

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
  @Getter private final List<DexRegisterType> ParameterTypes;

  public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags,
                   DexType returnType, List<DexRegisterType> parameterTypes) {
    ParentClass = parent;
    Name = name;
    AccessFlagSet = Utils.getNonNullAccessFlagSet(accessFlags);
    ReturnType = returnType;
    ParameterTypes = (parameterTypes == null) ? new LinkedList<DexRegisterType>() : parameterTypes;
  }

  protected static List<DexRegisterType> parseParameterTypes(TypeListItem params, DexParsingCache cache) throws UnknownTypeException {
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
         DexType.parse(methodInfo.method.getPrototype().getReturnType().getTypeDescriptor(), parent.getParentFile().getParsingCache()),
         parseParameterTypes(methodInfo.method.getPrototype().getParameters(), parent.getParentFile().getParsingCache()));
  }

  public boolean isStatic() {
    return AccessFlagSet.contains(AccessFlags.STATIC);
  }

  public abstract boolean isVirtual();

  public abstract void instrument();
}
