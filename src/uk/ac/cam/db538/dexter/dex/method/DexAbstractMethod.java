package uk.ac.cam.db538.dexter.dex.method;

import java.util.Set;

import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;

public class DexAbstractMethod extends DexMethod {

  public DexAbstractMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype, Set<DexAnnotation> annotations) {
    super(parent, name, accessFlags, prototype, annotations);
  }

  public DexAbstractMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations) {
    super(parent, methodInfo, encodedAnnotations);
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
  public void transformSSA() {
  }
}
