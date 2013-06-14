package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.apache.bcel.classfile.AnnotationEntry;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.AnnotationVisibility;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.EncodedValue.EncodedValue;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexAnnotation {

  @Getter private final DexClassType type;
  @Getter private final AnnotationVisibility visibility;
  private final List<String> paramNames;
  private final List<EncodedValue> paramValues;

  public DexAnnotation(DexClassType type, AnnotationVisibility visibility) {
    this.type = type;
    this.visibility = visibility;
    // Order of parameters matter, so it can't be stored in a hash table.
    this.paramNames = new ArrayList<String>();
    this.paramValues = new ArrayList<EncodedValue>();
  }

  public DexAnnotation(AnnotationItem anno, DexParsingCache cache) {
    this(DexClassType.parse(anno.getEncodedAnnotation().annotationType.getTypeDescriptor(), cache),
         anno.getVisibility());

    val encAnno = anno.getEncodedAnnotation();
    int len = encAnno.names.length;
    for (int i = 0; i < len; ++i)
      addParam(encAnno.names[i].getStringValue(), encAnno.values[i]);
  }

  public DexAnnotation(AnnotationEntry anno, DexParsingCache cache) {
    this(DexClassType.parse(anno.getAnnotationType(), cache),
         (anno.isRuntimeVisible()) ? AnnotationVisibility.RUNTIME : AnnotationVisibility.SYSTEM);

    // TODO: load parameters of the annotation
    // not necessary though, this is used to load android.jar
  }

  public void addParam(String name, EncodedValue value) {
	  paramNames.add(name);
	  paramValues.add(value);
  }

  public List<String> getParamNames() {
	return Collections.unmodifiableList(paramNames);
  }
  
  public List<EncodedValue> getParamValues() {
	return Collections.unmodifiableList(paramValues);
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

  public static List<Set<DexAnnotation>> parseAll(AnnotationSetRefList annotations, DexParsingCache cache) {
	    if (annotations == null)
	      return new ArrayList<Set<DexAnnotation>>();

	    val list = new ArrayList<Set<DexAnnotation>>();

	    for (val anno : annotations.getAnnotationSets())
	      list.add(parseAll(anno, cache));

	    return list;
	  }

  public AnnotationItem writeToFile(DexFile outFile, DexAssemblingCache cache) {
    int paramCount = paramNames.size();
    int paramIndex = 0;
    val paramNames = new StringIdItem[paramCount];
    val paramValues = new EncodedValue[paramCount];
    for (int i = 0; i < paramCount; i++) {
      paramNames[paramIndex] = cache.getStringConstant(this.paramNames.get(i));
      paramValues[paramIndex] = DexEncodedValue.cloneEncodedValue(this.paramValues.get(i), cache);
      paramIndex++;
    }

    val subValue = new AnnotationEncodedSubValue(cache.getType(type), paramNames, paramValues);
    return AnnotationItem.internAnnotationItem(outFile, visibility, subValue);
  }
}
