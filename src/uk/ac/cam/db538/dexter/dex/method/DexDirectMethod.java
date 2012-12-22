package uk.ac.cam.db538.dexter.dex.method;

import java.util.Set;

import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;

public class DexDirectMethod extends DexMethodWithCode {

  public DexDirectMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations) {
    super(parent, methodInfo, encodedAnnotations);
  }

  public DexDirectMethod(DexClass parent, String name,
                         Set<AccessFlags> accessFlags, DexPrototype prototype, DexCode code,
                         Set<DexAnnotation> annotations) {
    super(parent, name, accessFlags, prototype, code, annotations, true);
  }

  public Opcode_Invoke getCallType() {
    if (isStatic())
      return Opcode_Invoke.Static;
    else
      return Opcode_Invoke.Direct;
  }
}
