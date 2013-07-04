package uk.ac.cam.db538.dexter.dex.type;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class DexPrototype {

  @Getter private final DexType returnType;
  private final List<DexRegisterType> _parameterTypes;
  @Getter private final List<DexRegisterType> parameterTypes;

  public DexPrototype(DexType returnType, List<DexRegisterType> argTypes) {
    this.returnType = returnType;
    this._parameterTypes = (argTypes == null) ? new LinkedList<DexRegisterType>() : argTypes;
    this.parameterTypes = Collections.unmodifiableList(_parameterTypes);
  }

  public static DexPrototype parse(ProtoIdItem protoItem, DexTypeCache cache) {
	  String desc = protoItem.getPrototypeString();
	  System.out.println(desc);
	  
	  DexPrototype result = cache.getCachedPrototype(desc);
	  if (result == null) {
		  result = new DexPrototype(parseReturnType(protoItem.getReturnType(), cache),
				  					parseArgumentTypes(protoItem.getParameters(), cache));
		  cache.putCachedPrototype(desc, result);
	  }
	  
	  return result;
  }

  private static DexType parseReturnType(TypeIdItem item, DexTypeCache cache) {
    return DexType.parse(item.getTypeDescriptor(), cache);
  }

  private static List<DexRegisterType> parseArgumentTypes(TypeListItem params, DexTypeCache cache) {
    val list = new LinkedList<DexRegisterType>();
    if (params != null) {
      for (val type : params.getTypes())
        list.add(DexRegisterType.parse(type.getTypeDescriptor(), cache));
    }
    return list;
  }

  public int countParamWords(boolean isStatic) {
    int totalWords = 0;
    if (!isStatic)
      totalWords += DexClassType.TypeSize.getRegisterCount();
    for (val param : _parameterTypes)
      totalWords += param.getRegisters();
    return totalWords;
  }

  public int getParameterCount(boolean isStatic) {
    return _parameterTypes.size() + (isStatic ? 0 : 1);
  }

  public int getParameterRegisterId(int paramId, int registerCount, boolean isStatic) {
    return getFirstParameterRegisterIndex(paramId, isStatic) + registerCount - countParamWords(isStatic);
  }

  public int getFirstParameterRegisterIndex(int paramId, boolean isStatic) {
    if (paramId == 0)
      return 0;

    int regId = 0;

    if (!isStatic) {
      regId += DexClassType.TypeSize.getRegisterCount();
      paramId--;
    }

    for (int i = 0; i < paramId; ++i)
      regId += _parameterTypes.get(i).getRegisters();

    return regId;
  }

  public DexRegisterType getParameterType(int paramId, boolean isStatic, DexClass clazz) {
    if (!isStatic) {
      if (paramId == 0)
        return clazz.getType();
      else
        paramId--;
    }
    return _parameterTypes.get(paramId);
  }

  public NoDuplicatesList<DexRegister> generateParameterRegisters(boolean isStatic) {
    val regs = new NoDuplicatesList<DexRegister>();

    val paramWords = this.countParamWords(isStatic);
    for (int i = 0; i < paramWords; ++i)
      regs.add(new DexParameterRegister(i));

    return regs;
  }

  public List<DexRegister> generateArgumentTaintStoringRegisters(List<DexRegister> argumentRegisters, boolean isStatic, DexCode_InstrumentationState state) {
    val argStoreRegs = new LinkedList<DexRegister>();

    int i = isStatic ? 0 : 1;
    for (val paramType : _parameterTypes) {
      if (paramType instanceof DexPrimitiveType)
        for (int j = 0; j < paramType.getRegisters(); ++j)
          argStoreRegs.add(state.getTaintRegister(argumentRegisters.get(i + j)));
      i += paramType.getRegisters();
    }

    return argStoreRegs;
  }

  public static Cache<DexPrototype, ProtoIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<DexPrototype, ProtoIdItem>() {
      @Override
      protected ProtoIdItem createNewEntry(DexPrototype prototype) {
        return ProtoIdItem.internProtoIdItem(
                 outFile,
                 cache.getType(prototype.getReturnType()),
                 cache.getTypeList(prototype.getParameterTypes()));
      }
    };
  }

  public boolean hasPrimitiveArgument() {
    for (val paramType : _parameterTypes)
      if (paramType instanceof DexPrimitiveType)
        return true;
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
             + ((_parameterTypes == null) ? 0 : _parameterTypes.hashCode());
    result = prime * result
             + ((returnType == null) ? 0 : returnType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof DexPrototype))
      return false;
    DexPrototype other = (DexPrototype) obj;
    if (_parameterTypes == null) {
      if (other._parameterTypes != null)
        return false;
    } else if (!_parameterTypes.equals(other._parameterTypes))
      return false;
    if (returnType == null) {
      if (other.returnType != null)
        return false;
    } else if (!returnType.equals(other.returnType))
      return false;
    return true;
  }
  
  public String getDescriptor() {
	  StringBuilder sb = new StringBuilder();
	  sb.append("(");
	  for(val parameter : _parameterTypes) {
		  sb.append(parameter.getDescriptor());
	  }
	  sb.append(")");
	  sb.append(returnType.getDescriptor());
	  return sb.toString();
  }
  
  @Override
  public String toString() {
	  return getDescriptor();
  }
}
