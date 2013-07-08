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
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;

public class DexVirtualMethod extends DexMethodWithCode {

  public DexVirtualMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, AnnotationSetRefList paramAnnotations) {
    super(parent, methodInfo, encodedAnnotations, paramAnnotations);
  }

  public DexVirtualMethod(DexClass parent, String name,
                          Set<AccessFlags> accessFlags, DexPrototype prototype, DexCode code,
                          Set<DexAnnotation> annotations,
                          List<Set<DexAnnotation>> paramAnotations,
                          boolean direct) {
    super(parent, name, accessFlags, prototype, code, annotations, paramAnotations, direct);
  }
}
