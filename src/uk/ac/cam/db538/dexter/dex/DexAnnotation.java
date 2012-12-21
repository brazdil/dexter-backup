package uk.ac.cam.db538.dexter.dex;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.apache.bcel.classfile.AnnotationEntry;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationVisibility;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.EncodedValue.EncodedValue;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexAnnotation {

  @Getter private final DexClassType type;
  @Getter private final AnnotationVisibility visibility;
  private final Map<String, EncodedValue> params;

  public DexAnnotation(DexClassType type, AnnotationVisibility visibility) {
    this.type = type;
    this.visibility = visibility;
    this.params = new HashMap<String, EncodedValue>();
  }

  public DexAnnotation(AnnotationItem anno, DexParsingCache cache) {
    this(cache.getClassType(anno.getEncodedAnnotation().annotationType.getTypeDescriptor()),
         anno.getVisibility());

    val encAnno = anno.getEncodedAnnotation();
    int len = encAnno.names.length;
    for (int i = 0; i < len; ++i)
      addParam(encAnno.names[i].getStringValue(), encAnno.values[i]);
  }

  public DexAnnotation(AnnotationEntry anno, DexParsingCache cache) {
    this(cache.getClassType(anno.getAnnotationType()),
         (anno.isRuntimeVisible()) ? AnnotationVisibility.RUNTIME : AnnotationVisibility.SYSTEM);

    // TODO: load parameters of the annotation
    // not necessary though, this is used to load android.jar
  }

  public void addParam(String name, EncodedValue val) {
    params.put(name, val);
  }

  public Map<String, EncodedValue> getParams() {
    return Collections.unmodifiableMap(params);
  }

  public static Set<DexAnnotation> parseAll(AnnotationSetItem annotations, DexParsingCache cache) {
    if (annotations == null)
      return new HashSet<DexAnnotation>();

    val items = annotations.getAnnotations();
    val list = new HashSet<DexAnnotation>(items.length);

    for (val anno : items)
      list.add(new DexAnnotation(anno, cache));

    return list;
  }

  public AnnotationItem writeToFile(DexFile outFile, DexAssemblingCache cache) {
    int paramCount = params.size();
    int paramIndex = 0;
    val paramNames = new StringIdItem[paramCount];
    val paramValues = new EncodedValue[paramCount];
    for (val param : params.entrySet()) {
      paramNames[paramIndex] = cache.getStringConstant(param.getKey());
      paramValues[paramIndex] = param.getValue();
      paramIndex++;
    }

    val subValue = new AnnotationEncodedSubValue(cache.getType(type), paramNames, paramValues);
    return AnnotationItem.internAnnotationItem(outFile, visibility, subValue);
  }
}
