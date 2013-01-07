package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public class DexField {

  @Getter @Setter private DexClass parentClass;
  @Getter private final String name;
  @Getter private final DexRegisterType type;
  private final Set<AccessFlags> accessFlagSet;
  private final Set<DexAnnotation> annotations;

  public DexField(DexClass parent, String name, DexRegisterType type, Set<AccessFlags> accessFlags, Set<DexAnnotation> annotations) {
    this.parentClass = parent;
    this.name = name;
    this.type = type;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.annotations = (annotations == null) ? new HashSet<DexAnnotation>() : annotations;

    parentClass.getParentFile().getClassHierarchy().addDeclaredField(
      parentClass.getType(),
      name,
      type,
      isStatic(),
      isPrivate());
  }

  public DexField(DexClass parent, EncodedField fieldInfo, AnnotationSetItem encodedAnnotations) {
    this(parent,
         StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
         DexRegisterType.parse(fieldInfo.field.getFieldType().getTypeDescriptor(), parent.getParentFile().getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)),
         DexAnnotation.parseAll(encodedAnnotations, parent.getParentFile().getParsingCache()));
  }

  public Set<AccessFlags> getAccessFlagSet() {
    return Collections.unmodifiableSet(accessFlagSet);
  }

  public boolean isStatic() {
    return accessFlagSet.contains(AccessFlags.STATIC);
  }

  public boolean isPrivate() {
    return accessFlagSet.contains(AccessFlags.PRIVATE);
  }

  public Set<DexAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(DexAnnotation anno) {
    annotations.add(anno);
  }

  public EncodedField writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val fieldItem = cache.getField(parentClass.getType(), type, name);
    val accessFlags = DexUtils.assembleAccessFlags(accessFlagSet);

    return new EncodedField(fieldItem, accessFlags);
  }

  public static Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<Triple<DexClassType, DexRegisterType, String>, FieldIdItem>() {
      @Override
      protected FieldIdItem createNewEntry(Triple<DexClassType, DexRegisterType, String> key) {
        return FieldIdItem.internFieldIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getType(key.getValB()),
                 cache.getStringConstant(key.getValC()));
      }
    };
  }

  public FieldAnnotation assembleAnnotations(DexFile outFile, DexAssemblingCache cache) {
    val annoList = new ArrayList<AnnotationItem>(annotations.size());
    for (val anno : annotations)
      annoList.add(anno.writeToFile(outFile, cache));

    val annoSet = AnnotationSetItem.internAnnotationSetItem(outFile, annoList);
    val fieldAnno = new FieldAnnotation(cache.getField(parentClass.getType(), type, name), annoSet);

    return fieldAnno;
  }

  private String generateTaintFieldName() {
    long suffix = 0L;
    String baseFieldName = name + "$T";
    String fieldName = baseFieldName;
    while (parentClass.containsField(fieldName))
      fieldName = baseFieldName + (suffix++);
    return fieldName;
  }

  public DexField instrument() {
    if (type instanceof DexPrimitiveType) {
      val newName = generateTaintFieldName();
      val newType = DexPrimitiveType.parse("I", parentClass.getParentFile().getParsingCache());

      val newField = new DexField(parentClass, newName, newType, accessFlagSet, annotations);
      parentClass.addField(newField);

      return newField;
    } else
      return null;
  }
}
