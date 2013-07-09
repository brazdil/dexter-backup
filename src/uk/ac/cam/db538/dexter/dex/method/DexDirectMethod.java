package uk.ac.cam.db538.dexter.dex.method;

import java.util.List;
import java.util.Set;

import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;

public class DexDirectMethod extends DexMethodWithCode {

  public DexDirectMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, AnnotationSetRefList annotationSetRefList) {
    super(parent, methodInfo, encodedAnnotations, annotationSetRefList);
  }

  public DexDirectMethod(DexClass parent, String name,
                         Set<AccessFlags> accessFlags, DexPrototype prototype, DexCode code,
                         Set<DexAnnotation> annotations, List<Set<DexAnnotation>> paramAnotations) {
    super(parent, name, accessFlags, prototype, code, annotations, paramAnotations, true);
  }

  public Opcode_Invoke getCallType() {
    if (isStatic())
      return Opcode_Invoke.Static;
    else
      return Opcode_Invoke.Direct;
  }
}
