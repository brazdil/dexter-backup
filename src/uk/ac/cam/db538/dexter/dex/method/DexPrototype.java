package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
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
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class DexPrototype {

  @Getter private final DexType returnType;
  private final List<DexRegisterType> parameterTypes;

  public DexPrototype(DexType returnType, List<DexRegisterType> argTypes) {
    this.returnType = returnType;
    this.parameterTypes = (argTypes == null) ? new LinkedList<DexRegisterType>() : argTypes;
  }

  public DexPrototype(ProtoIdItem protoItem, DexParsingCache cache) {
    this(parseReturnType(protoItem.getReturnType(), cache),
         parseArgumentTypes(protoItem.getParameters(), cache));
  }

  public DexPrototype(String signature, DexParsingCache cache) {
    val leftBracket = signature.indexOf('(');
    val rightBracket = signature.indexOf(')');

    val returnStr = signature.substring(rightBracket + 1);
    val paramStr = signature.substring(leftBracket + 1, rightBracket);

    this.returnType = DexType.parse(returnStr, cache);
    this.parameterTypes = parseRegisterTypeList(paramStr, cache);
  }

  public List<DexRegisterType> getParameterTypes() {
    return Collections.unmodifiableList(parameterTypes);
  }

  private static DexType parseReturnType(TypeIdItem item, DexParsingCache cache) {
    return DexType.parse(item.getTypeDescriptor(), cache);
  }

  private static List<DexRegisterType> parseArgumentTypes(TypeListItem params, DexParsingCache cache) {
    val list = new LinkedList<DexRegisterType>();
    if (params != null) {
      for (val type : params.getTypes())
        list.add(DexRegisterType.parse(type.getTypeDescriptor(), cache));
    }
    return list;
  }

  private static String getFirstTypeSubstring(String str) {
    String result;
    if (str.startsWith("L")) {
      val semicolon = str.indexOf(';');
      result = str.substring(0, semicolon + 1);
    } else if (str.startsWith("[")) {
      result = "[" + getFirstTypeSubstring(str.substring(1));
    } else {
      result = str.substring(0, 1);
    }

    return result;
  }

  private static List<DexRegisterType> parseRegisterTypeList(String str, DexParsingCache cache) {
    val list = new ArrayList<DexRegisterType>();

    while (!str.isEmpty()) {
      val firstType = getFirstTypeSubstring(str);
      str = str.substring(firstType.length());
      list.add(DexRegisterType.parse(firstType, cache));
    }

    return list;
  }

  public int countParamWords(boolean isStatic) {
    int totalWords = 0;
    if (!isStatic)
      totalWords += DexClassType.TypeSize.getRegisterCount();
    for (val param : parameterTypes)
      totalWords += param.getRegisters();
    return totalWords;
  }

  public int getParameterCount(boolean isStatic) {
    return parameterTypes.size() + (isStatic ? 0 : 1);
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
      regId += parameterTypes.get(i).getRegisters();

    return regId;
  }

  public DexRegisterType getParameterType(int paramId, boolean isStatic, DexClass clazz) {
    if (!isStatic) {
      if (paramId == 0)
        return clazz.getType();
      else
        paramId--;
    }
    return parameterTypes.get(paramId);
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
    for (val paramType : parameterTypes) {
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
    for (val paramType : parameterTypes)
      if (paramType instanceof DexPrimitiveType)
        return true;
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
             + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
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
    if (parameterTypes == null) {
      if (other.parameterTypes != null)
        return false;
    } else if (!parameterTypes.equals(other.parameterTypes))
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
	  for(val parameter : parameterTypes) {
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
