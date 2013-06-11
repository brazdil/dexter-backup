package uk.ac.cam.db538.dexter.dex.method;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;

public class DexAbstractMethod extends DexMethod {

  public DexAbstractMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype, Set<DexAnnotation> annotations, List<Set<DexAnnotation>> paramAnotations) {
    super(parent, name, accessFlags, prototype, annotations, paramAnotations);
  }

  public DexAbstractMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, AnnotationSetRefList annotationSetRefList) {
    super(parent, methodInfo, encodedAnnotations, annotationSetRefList);
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @Override
  public void instrument(DexInstrumentationCache cache) { }

  @Override
  protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
    return null;
  }

  @Override
  public void markMethodOriginal() { }

  @Override
  public void transformSSA() { }
  
  @Override
  public void countInstructions(HashMap<Class, Integer> count) { }
}
