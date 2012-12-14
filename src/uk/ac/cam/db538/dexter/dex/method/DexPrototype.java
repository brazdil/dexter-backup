package uk.ac.cam.db538.dexter.dex.method;

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
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexDouble;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexLong;
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

  public int getParameterRegisterId(int paramId, int registerCount, boolean isStatic, DexClass clazz) {
    int regId = registerCount - getParameterCount(isStatic);
    if (paramId == 0)
      return regId;
    else if (!isStatic) {
      regId += clazz.getType().getRegisters();
      paramId--;
    }

    for (int i = 0; i < paramId; ++i)
      regId = parameterTypes.get(i).getRegisters();

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
      regs.add(new DexRegister());

    return regs;
  }

  public DexPrototype getInstrumentedPrototype(DexInstrumentationCache cache) {
    // modify the return type
    DexType newReturnType = null;
    // change only primitives
    if (returnType instanceof DexPrimitiveType) {
      // turn long into Long, double into Double
      if (((DexPrimitiveType) returnType).isWide()) {
        if (returnType instanceof DexLong)
          newReturnType = cache.getParsingCache().getClassType("Ljava/lang/Long;");
        else if (returnType instanceof DexDouble)
          newReturnType = cache.getParsingCache().getClassType("Ljava/lang/Double;");
        // turn all the short ones into long
      } else
        newReturnType = DexPrimitiveType.parse("J");
      // leave objects alone
    } else
      newReturnType = returnType;

    // add extra parameters for passing taint of primitives
    // one int parameter per primitive
    val newParameterTypes = new LinkedList<DexRegisterType>(parameterTypes);
    val typeInteger = DexPrimitiveType.parse("I");
    for (val paramType : parameterTypes)
      if (paramType instanceof DexPrimitiveType)
        newParameterTypes.add(typeInteger);

    return new DexPrototype(newReturnType, newParameterTypes);
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
}
