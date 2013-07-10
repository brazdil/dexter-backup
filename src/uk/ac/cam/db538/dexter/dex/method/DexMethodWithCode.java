package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.AnnotationVisibility;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

import com.rx201.dx.translator.DexCodeGeneration;

public abstract class DexMethodWithCode extends DexMethod {

  @Getter protected DexCode code;
  @Getter private final boolean direct;
  @Getter private int registerCount;
  
  private final List<DexRegister> parameterRegisters;
  private final Map<DexRegister, DexRegister> parameterRegistersMappings;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexPrototype prototype, DexCode code,
                           Set<DexAnnotation> annotations,
                           List<Set<DexAnnotation>> paramAnnotations,
                           boolean direct) {
    super(parent, name, accessFlags, prototype, annotations, paramAnnotations);
    this.code = code;
    this.direct = direct;
    this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    this.parameterRegistersMappings = new HashMap<DexRegister, DexRegister>();

    if (this.code != null)
      this.code.setParentMethod(this);
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, AnnotationSetRefList paramAnnotations) {
    super(parent, methodInfo, encodedAnnotations, paramAnnotations);
    this.direct = methodInfo.isDirect();
    this.parameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    this.parameterRegistersMappings = new HashMap<DexRegister, DexRegister>();

    if (methodInfo.codeItem != null) {
      this.code = new DexCode(methodInfo.codeItem, this, parent.getParentFile().getTypeCache());
      this.registerCount = methodInfo.codeItem.getRegisterCount();
      
      val prototype = this.getPrototype();
      val isStatic = this.isStatic();
      val clazz = this.getParentClass();

      // create the parameter-register mappings
      val regCount = methodInfo.codeItem.getRegisterCount();
      val paramCount = prototype.getParameterCount(isStatic);
      for (int i = 0; i < paramCount; ++i) {
        val paramRegId = prototype.getParameterRegisterId(i, regCount, isStatic);
        val paramType = prototype.getParameterType(i, isStatic, clazz);
        switch (paramType.getTypeSize()) {
        case SINGLE:
          val regSingle = code.getRegisterByOriginalNumber(paramRegId);
          addParameterMapping_Single(i, regSingle);
          break;
        case WIDE:
          val regWide1 = code.getRegisterByOriginalNumber(paramRegId);
          val regWide2 = code.getRegisterByOriginalNumber(paramRegId + 1);
          addParameterMapping_Wide(i, regWide1, regWide2);
          break;
        }
      }
    } else
      this.code = null;
  }

  private void addParameterMapping_Single(int paramIndex, DexRegister codeReg) {
//    if (!code.getUsedRegisters().contains(codeReg))
//      return;

    val paramType = this.getPrototype().getParameterType(paramIndex, this.isStatic(), this.getParentClass());

    val regIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());
    val paramReg = parameterRegisters.get(regIndex);

    val moveInsn = new DexInstruction_Move(code, codeReg, paramReg, paramType instanceof DexReferenceType);
    moveInsn.setAuxiliaryElement(true);
    code.insertBefore(moveInsn, code.getStartingLabel());

    if (parameterRegistersMappings.containsKey(paramReg))
      throw new RuntimeException("Multiple mappings of the same parameter");
    else
      parameterRegistersMappings.put(paramReg, codeReg);
  }

  private void addParameterMapping_Wide(int paramIndex, DexRegister codeReg1, DexRegister codeReg2) {
//    if (!code.getUsedRegisters().contains(codeReg1) && !code.getUsedRegisters().contains(codeReg2))
//      return;

    val firstRegIndex = this.getPrototype().getFirstParameterRegisterIndex(paramIndex, isStatic());

    val paramReg1 = parameterRegisters.get(firstRegIndex);
    val paramReg2 = parameterRegisters.get(firstRegIndex + 1);

    val moveInsn = new DexInstruction_MoveWide(code, codeReg1, codeReg2, paramReg1, paramReg2);
    moveInsn.setAuxiliaryElement(true);
    code.insertBefore(moveInsn, code.getStartingLabel());

    if (parameterRegistersMappings.containsKey(paramReg1) || parameterRegistersMappings.containsKey(paramReg2))
      throw new RuntimeException("Multiple mappings of the same parameter");
    else {
      parameterRegistersMappings.put(paramReg1, codeReg1);
      parameterRegistersMappings.put(paramReg2, codeReg2);
    }
  }

  public List<DexRegister> getParameterRegisters() {
    return Collections.unmodifiableList(parameterRegisters);
  }

  public List<DexRegister> getParameterMappedRegisters() {
    val list = new ArrayList<DexRegister>(parameterRegisters.size());

    for (val paramReg : parameterRegisters) {
      val codeReg = parameterRegistersMappings.get(paramReg);
//		  if (codeReg == null)
//			  throw new RuntimeException("Missing parameter register mapping (" + getParentClass().getType().getPrettyName() + "." + getName() + ")");
      list.add(codeReg);
    }

    return list;
  }

  @Override
  public boolean isVirtual() {
    return !direct;
  }

  @Override
  public void instrument(DexInstrumentationCache cache) {
    if (code != null) {
      code.instrument(cache);

      if (isVirtual())
        this.addAnnotation(
          new DexAnnotation(getParentFile().getInternalMethodAnnotation_Type(),
                            AnnotationVisibility.RUNTIME));
    }
  }

  
  @Override
  protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
    if (code == null)
      return null;

    DexCodeGeneration cg = new DexCodeGeneration(this);
	return cg.generateCodeItem(outFile);
  }

  @Override
  public void markMethodOriginal() {
    if (code != null)
      code.markAllInstructionsOriginal();
  }

  @Override
  public void countInstructions(HashMap<Class, Integer> count) {
    if (code != null)
      code.countInstructions(count);
  }
}
